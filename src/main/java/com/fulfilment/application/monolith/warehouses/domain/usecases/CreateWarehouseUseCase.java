package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  @jakarta.transaction.Transactional
  public void create(Warehouse warehouse) {
    warehouseValidator.validateBusinessUnitCodeUnique(warehouse.getBusinessUnitCode());

    Location loc = warehouseValidator.validateLocation(warehouse.getLocation());

    warehouseValidator.validateLocationFeasibility(loc, warehouse.getLocation());

    int newCapacity = warehouse.getCapacity() != null ? warehouse.getCapacity() : 0;
    int newStock = warehouse.getStock() != null ? warehouse.getStock() : 0;

    warehouseValidator.validateCapacity(loc, warehouse.getLocation(), newCapacity, null);

    if (newCapacity < newStock) {
      throw new WarehouseValidationException(
          "Warehouse capacity must be at least the stock amount");
    }

    warehouse.setCreationAt(ZonedDateTime.now());
    warehouseStore.create(warehouse);
  }
}
