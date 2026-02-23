package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseValidationException(
          "Warehouse not found: " + newWarehouse.businessUnitCode);
    }
    if (existing.archivedAt != null) {
      throw new WarehouseValidationException(
          "Cannot replace archived warehouse: " + newWarehouse.businessUnitCode);
    }

    // Location Validation
    Location loc = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (loc == null) {
      throw new WarehouseValidationException("Invalid or unknown location: " + newWarehouse.location);
    }

    // Location Feasibility: Check if location changes and max warehouses is reached
    if (!existing.location.equals(newWarehouse.location)) {
      long currentCount = warehouseStore.countActiveByLocation(newWarehouse.location);
      if (currentCount >= loc.maxNumberOfWarehouses) {
        throw new WarehouseValidationException(
            "Maximum number of warehouses (" + loc.maxNumberOfWarehouses
                + ") already reached for location: " + newWarehouse.location);
      }
    }

    // Capacity Accommodation: new capacity must accommodate stock from warehouse
    // being replaced
    int newCapacity = newWarehouse.capacity != null ? newWarehouse.capacity : 0;
    int existingStock = existing.stock != null ? existing.stock : 0;
    if (newCapacity < existingStock) {
      throw new WarehouseValidationException(
          "New warehouse capacity must accommodate existing stock (" + existingStock + ")");
    }

    // Capacity and Stock Validation: total capacity <= location max
    int currentTotalCapacity = warehouseStore.sumCapacityByLocation(newWarehouse.location);
    int existingCapacity = existing.location.equals(newWarehouse.location)
        ? (existing.capacity != null ? existing.capacity : 0)
        : 0;

    if (currentTotalCapacity - existingCapacity + newCapacity > loc.maxCapacity) {
      throw new WarehouseValidationException(
          "Total capacity would exceed location max capacity " + loc.maxCapacity
              + " for location: " + newWarehouse.location);
    }

    // Stock Matching: new warehouse stock must match previous warehouse stock
    int newStock = newWarehouse.stock != null ? newWarehouse.stock : 0;
    if (newStock != existingStock) {
      throw new WarehouseValidationException(
          "Stock of the new warehouse must match the previous warehouse stock (" + existingStock + ")");
    }

    newWarehouse.creationAt = existing.creationAt;
    warehouseStore.update(newWarehouse);
  }
}
