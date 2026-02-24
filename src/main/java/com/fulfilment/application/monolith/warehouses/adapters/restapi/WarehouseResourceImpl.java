package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class WarehouseResourceImpl implements WarehouseResource {

  private final CreateWarehouseOperation createWarehouseOperation;
  private final ArchiveWarehouseOperation archiveWarehouseOperation;
  private final WarehouseStore warehouseStore;

  public WarehouseResourceImpl(
      CreateWarehouseOperation createWarehouseOperation,
      ArchiveWarehouseOperation archiveWarehouseOperation,
      WarehouseStore warehouseStore) {
    this.createWarehouseOperation = createWarehouseOperation;
    this.archiveWarehouseOperation = archiveWarehouseOperation;
    this.warehouseStore = warehouseStore;
  }

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.listActive().stream()
        .map(this::toApi)
        .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = toDomain(data);
    if (domain.getBusinessUnitCode() == null || domain.getBusinessUnitCode().isBlank()) {
      throw new WebApplicationException("Warehouse id (business unit code) is required", 422);
    }
    try {
      createWarehouseOperation.create(domain);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
    return toApi(warehouseStore.findByBusinessUnitCode(domain.getBusinessUnitCode()));
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = warehouseStore
        .findByBusinessUnitCode(id);
    if (domain == null || domain.getArchivedAt() != null) {
      throw new WebApplicationException("Warehouse unit not found", 404);
    }
    return toApi(domain);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = warehouseStore
        .findByBusinessUnitCode(id);
    if (domain == null) {
      throw new WebApplicationException("Warehouse unit not found", 404);
    }
    try {
      archiveWarehouseOperation.archive(domain);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  private Warehouse toApi(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d) {
    Warehouse w = new Warehouse();
    w.setId(d.getBusinessUnitCode());
    w.setLocation(d.getLocation());
    w.setCapacity(d.getCapacity());
    w.setStock(d.getStock());
    return w;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(Warehouse api) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    d.setBusinessUnitCode(api.getId());
    d.setLocation(api.getLocation());
    d.setCapacity(api.getCapacity());
    d.setStock(api.getStock());
    return d;
  }
}
