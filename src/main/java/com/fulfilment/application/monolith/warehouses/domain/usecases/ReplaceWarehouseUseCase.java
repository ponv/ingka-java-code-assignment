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

    // Capacity Accommodation: new capacity must accommodate stock from warehouse being replaced
    int newCapacity = newWarehouse.capacity != null ? newWarehouse.capacity : 0;
    int existingStock = existing.stock != null ? existing.stock : 0;
    if (newCapacity < existingStock) {
      throw new WarehouseValidationException(
          "New warehouse capacity must accommodate existing stock (" + existingStock + ")");
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
