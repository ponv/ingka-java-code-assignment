package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.ZonedDateTime;

class ReplaceWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private WarehouseValidator validator;

    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new ReplaceWarehouseUseCase(warehouseStore, validator);
    }

    @Test
    void testReplaceSuccess() {
        Warehouse existing = new Warehouse();
        existing.setBusinessUnitCode("BU-1");
        existing.setLocation("LOC-1");
        existing.setCapacity(100);
        existing.setStock(50);
        existing.setCreationAt(ZonedDateTime.now());

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU-1");
        newWarehouse.setLocation("LOC-1");
        newWarehouse.setCapacity(150);
        newWarehouse.setStock(50);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);
        when(validator.validateLocation("LOC-1")).thenReturn(new Location("LOC-1", 1, 300));

        useCase.replace(newWarehouse);

        verify(validator).validateCapacity(any(), eq("LOC-1"), eq(150), eq(100));
        verify(warehouseStore).update(newWarehouse);
        assertEquals(existing.getCreationAt(), newWarehouse.getCreationAt());
    }

    @Test
    void testReplaceFailsIfNotFound() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU-1");

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);

        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));
    }

    @Test
    void testReplaceFailsIfArchived() {
        Warehouse existing = new Warehouse();
        existing.setBusinessUnitCode("BU-1");
        existing.setArchivedAt(ZonedDateTime.now());

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU-1");

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);

        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));
    }
}
