package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.ZonedDateTime;

class ArchiveWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;

    private ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    void testArchiveSuccess() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-1");

        Warehouse existing = new Warehouse();
        existing.setBusinessUnitCode("BU-1");

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);

        useCase.archive(warehouse);

        verify(warehouseStore).update(existing);
        assertNotNull(existing.getArchivedAt());
    }

    @Test
    void testArchiveFailsIfNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-1");

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);

        assertThrows(WarehouseValidationException.class, () -> useCase.archive(warehouse));
        verify(warehouseStore, never()).update(any());
    }

    @Test
    void testArchiveFailsIfAlreadyArchived() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-1");

        Warehouse existing = new Warehouse();
        existing.setBusinessUnitCode("BU-1");
        existing.setArchivedAt(ZonedDateTime.now());

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);

        assertThrows(WarehouseValidationException.class, () -> useCase.archive(warehouse));
        verify(warehouseStore, never()).update(any());
    }
}
