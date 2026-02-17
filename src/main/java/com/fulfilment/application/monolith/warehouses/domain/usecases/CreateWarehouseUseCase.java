package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Business Unit Code Verification: must not already exist
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WarehouseValidationException(
          "Business unit code already exists: " + warehouse.businessUnitCode);
    }

    // Location Validation: must be an existing valid location
    Location loc = locationResolver.resolveByIdentifier(warehouse.location);
    if (loc == null) {
      throw new WarehouseValidationException("Invalid or unknown location: " + warehouse.location);
    }

    // Warehouse Creation Feasibility: max number of warehouses at location not exceeded
    long currentCount = warehouseStore.countActiveByLocation(warehouse.location);
    if (currentCount >= loc.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses (" + loc.maxNumberOfWarehouses
              + ") already reached for location: " + warehouse.location);
    }

    // Capacity and Stock Validation: capacity <= location max, capacity >= stock
    int currentCapacity = warehouseStore.sumCapacityByLocation(warehouse.location);
    int newCapacity = warehouse.capacity != null ? warehouse.capacity : 0;
    int newStock = warehouse.stock != null ? warehouse.stock : 0;
    if (currentCapacity + newCapacity > loc.maxCapacity) {
      throw new WarehouseValidationException(
          "Total capacity would exceed location max capacity " + loc.maxCapacity
              + " for location: " + warehouse.location);
    }
    if (newCapacity < newStock) {
      throw new WarehouseValidationException(
          "Warehouse capacity must be at least the stock amount");
    }

    warehouse.creationAt = ZonedDateTime.now();
    warehouseStore.create(warehouse);
  }
}
