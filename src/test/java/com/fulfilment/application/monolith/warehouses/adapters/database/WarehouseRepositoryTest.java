package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @Test
    @Transactional
    void testCreateAndFind() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("TEST-BU-001");
        warehouse.setLocation("TEST-LOC");
        warehouse.setCapacity(100);
        warehouse.setStock(0);

        warehouseRepository.create(warehouse);

        Warehouse found = warehouseRepository.findByBusinessUnitCode("TEST-BU-001");
        assertNotNull(found);
        assertEquals("TEST-LOC", found.getLocation());
        assertEquals(100, found.getCapacity());
    }

    @Test
    @Transactional
    void testUpdate() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("TEST-BU-002");
        warehouse.setLocation("TEST-LOC");
        warehouse.setCapacity(100);
        warehouse.setStock(0);
        warehouseRepository.create(warehouse);

        Warehouse toUpdate = warehouseRepository.findByBusinessUnitCode("TEST-BU-002");
        toUpdate.setCapacity(200);
        warehouseRepository.update(toUpdate);

        Warehouse updated = warehouseRepository.findByBusinessUnitCode("TEST-BU-002");
        assertEquals(200, updated.getCapacity());
    }

    @Test
    void testCountActiveByLocation() {
        // Rely on import.sql but don't modify it
        long count = warehouseRepository.countActiveByLocation("ZWOLLE-001");
        assertEquals(1, count);
    }

    @Test
    @Transactional
    void testSumCapacityByLocation() {
        // Rely on import.sql but don't modify it
        int sum = warehouseRepository.sumCapacityByLocation("ZWOLLE-001");
        assertEquals(100, sum);
    }

    @Test
    @Transactional
    void testSumCapacityWithNullCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("NULL-CAP-BU");
        warehouse.setLocation("NULL-LOC");
        warehouse.setCapacity(null);
        warehouseRepository.create(warehouse);

        int sum = warehouseRepository.sumCapacityByLocation("NULL-LOC");
        assertEquals(0, sum);
    }

    @Test
    void testListActive() {
        List<Warehouse> active = warehouseRepository.listActive();
        assertNotNull(active);
        // At least the 3 from import.sql should be active
        org.hamcrest.MatcherAssert.assertThat(active.size(), org.hamcrest.Matchers.greaterThanOrEqualTo(3));
    }
}
