package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  @jakarta.transaction.Transactional
  public void replace(Warehouse newWarehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode());
    if (existing == null) {
      throw new WarehouseValidationException(
          "Warehouse not found: " + newWarehouse.getBusinessUnitCode());
    }
    if (existing.getArchivedAt() != null) {
      throw new WarehouseValidationException(
          "Cannot replace archived warehouse: " + newWarehouse.getBusinessUnitCode());
    }

    Location loc = warehouseValidator.validateLocation(newWarehouse.getLocation());

    if (!existing.getLocation().equals(newWarehouse.getLocation())) {
      warehouseValidator.validateLocationFeasibility(loc, newWarehouse.getLocation());
    }

    int newCapacity = newWarehouse.getCapacity() != null ? newWarehouse.getCapacity() : 0;
    int existingStock = existing.getStock() != null ? existing.getStock() : 0;
    if (newCapacity < existingStock) {
      throw new WarehouseValidationException(
          "New warehouse capacity must accommodate existing stock (" + existingStock + ")");
    }

    Integer existingCapacity = existing.getLocation().equals(newWarehouse.getLocation())
        ? existing.getCapacity()
        : null;

    warehouseValidator.validateCapacity(loc, newWarehouse.getLocation(), newCapacity, existingCapacity);

    int newStock = newWarehouse.getStock() != null ? newWarehouse.getStock() : 0;
    if (newStock != existingStock) {
      throw new WarehouseValidationException(
          "Stock of the new warehouse must match the previous warehouse stock (" + existingStock + ")");
    }

    newWarehouse.setCreationAt(existing.getCreationAt());
    warehouseStore.update(newWarehouse);
  }
}
