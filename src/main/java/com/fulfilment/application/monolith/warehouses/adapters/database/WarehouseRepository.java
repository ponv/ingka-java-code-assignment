package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final String BUSINESS_UNIT_CODE = "businessUnitCode";

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse db = toDb(warehouse);
    db.setCreatedAt(warehouse.getCreationAt() != null
        ? warehouse.getCreationAt().toLocalDateTime()
        : LocalDateTime.now());
    persist(db);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse db = find(BUSINESS_UNIT_CODE, warehouse.getBusinessUnitCode()).firstResult();
    if (db == null) {
      throw new IllegalArgumentException("Warehouse not found: " + warehouse.getBusinessUnitCode());
    }
    db.setLocation(warehouse.getLocation());
    db.setCapacity(warehouse.getCapacity());
    db.setStock(warehouse.getStock());
    db.setArchivedAt(warehouse.getArchivedAt() != null
        ? warehouse.getArchivedAt().toLocalDateTime()
        : null);
    persist(db);
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse db = find(BUSINESS_UNIT_CODE, warehouse.getBusinessUnitCode()).firstResult();
    if (db != null) {
      delete(db);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse db = find(BUSINESS_UNIT_CODE, buCode).firstResult();
    return db != null ? toDomain(db) : null;
  }

  @Override
  public long countActiveByLocation(String locationId) {
    return count("location = ?1 and archivedAt is null", locationId);
  }

  @Override
  public int sumCapacityByLocation(String locationId) {
    List<DbWarehouse> list = list("location = ?1 and archivedAt is null", locationId);
    return list.stream().mapToInt(d -> d.getCapacity() != null ? d.getCapacity() : 0).sum();
  }

  @Override
  public List<Warehouse> listActive() {
    return list("archivedAt is null", io.quarkus.panache.common.Sort.ascending(BUSINESS_UNIT_CODE))
        .stream()
        .map(WarehouseRepository::toDomain)
        .toList();
  }

  private static DbWarehouse toDb(Warehouse w) {
    DbWarehouse db = new DbWarehouse();
    db.setBusinessUnitCode(w.getBusinessUnitCode());
    db.setLocation(w.getLocation());
    db.setCapacity(w.getCapacity());
    db.setStock(w.getStock());
    return db;
  }

  private static Warehouse toDomain(DbWarehouse db) {
    Warehouse w = new Warehouse();
    w.setBusinessUnitCode(db.getBusinessUnitCode());
    w.setLocation(db.getLocation());
    w.setCapacity(db.getCapacity());
    w.setStock(db.getStock());
    w.setCreationAt(db.getCreatedAt() != null ? db.getCreatedAt().atZone(ZoneOffset.UTC) : null);
    w.setArchivedAt(db.getArchivedAt() != null ? db.getArchivedAt().atZone(ZoneOffset.UTC) : null);
    return w;
  }
}
