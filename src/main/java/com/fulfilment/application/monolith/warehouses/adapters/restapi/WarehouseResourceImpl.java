package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class WarehouseResourceImpl implements WarehouseResource {

  @Inject com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation createWarehouseOperation;
  @Inject com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore warehouseStore;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.listActive().stream()
        .map(this::toApi)
        .collect(Collectors.toList());
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = toDomain(data);
    if (domain.businessUnitCode == null || domain.businessUnitCode.isBlank()) {
      throw new WebApplicationException("Warehouse id (business unit code) is required", 422);
    }
    try {
      createWarehouseOperation.create(domain);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
    return toApi(warehouseStore.findByBusinessUnitCode(domain.businessUnitCode));
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = warehouseStore.findByBusinessUnitCode(id);
    if (domain == null || domain.archivedAt != null) {
      throw new WebApplicationException("Warehouse unit not found", 404);
    }
    return toApi(domain);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = warehouseStore.findByBusinessUnitCode(id);
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
    w.setId(d.businessUnitCode);
    w.setLocation(d.location);
    w.setCapacity(d.capacity);
    w.setStock(d.stock);
    return w;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(Warehouse api) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    d.businessUnitCode = api.getId();
    d.location = api.getLocation();
    d.capacity = api.getCapacity();
    d.stock = api.getStock();
    return d;
  }
}
