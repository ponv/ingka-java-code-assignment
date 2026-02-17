package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse db = toDb(warehouse);
    db.createdAt = warehouse.creationAt != null
        ? warehouse.creationAt.toLocalDateTime()
        : LocalDateTime.now();
    persist(db);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse db = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db == null) {
      throw new IllegalArgumentException("Warehouse not found: " + warehouse.businessUnitCode);
    }
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.archivedAt = warehouse.archivedAt != null
        ? warehouse.archivedAt.toLocalDateTime()
        : null;
    persist(db);
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse db = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db != null) {
      delete(db);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse db = find("businessUnitCode", buCode).firstResult();
    return db != null ? toDomain(db) : null;
  }

  @Override
  public long countActiveByLocation(String locationId) {
    return count("location = ?1 and archivedAt is null", locationId);
  }

  @Override
  public int sumCapacityByLocation(String locationId) {
    List<DbWarehouse> list = list("location = ?1 and archivedAt is null", locationId);
    return list.stream().mapToInt(d -> d.capacity != null ? d.capacity : 0).sum();
  }

  @Override
  public List<Warehouse> listActive() {
    return list("archivedAt is null", io.quarkus.panache.common.Sort.ascending("businessUnitCode"))
        .stream()
        .map(WarehouseRepository::toDomain)
        .collect(Collectors.toList());
  }

  private static DbWarehouse toDb(Warehouse w) {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = w.businessUnitCode;
    db.location = w.location;
    db.capacity = w.capacity;
    db.stock = w.stock;
    return db;
  }

  private static Warehouse toDomain(DbWarehouse db) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = db.businessUnitCode;
    w.location = db.location;
    w.capacity = db.capacity;
    w.stock = db.stock;
    w.creationAt = db.createdAt != null ? db.createdAt.atZone(ZoneOffset.UTC) : null;
    w.archivedAt = db.archivedAt != null ? db.archivedAt.atZone(ZoneOffset.UTC) : null;
    return w;
  }
}
