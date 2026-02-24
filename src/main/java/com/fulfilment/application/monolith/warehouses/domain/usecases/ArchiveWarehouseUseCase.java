package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  @jakarta.transaction.Transactional
  public void archive(Warehouse warehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.getBusinessUnitCode());
    if (existing == null) {
      throw new WarehouseValidationException(
          "Warehouse not found: " + warehouse.getBusinessUnitCode());
    }
    if (existing.getArchivedAt() != null) {
      throw new WarehouseValidationException(
          "Warehouse already archived: " + warehouse.getBusinessUnitCode());
    }

    existing.setArchivedAt(ZonedDateTime.now());
    warehouseStore.update(existing);
  }
}
