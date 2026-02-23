package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.ZonedDateTime;

public class ReplaceWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private LocationResolver locationResolver;

    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
    }

    @Test
    public void testReplaceSuccess() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "BU-1";
        existing.location = "LOC-1";
        existing.capacity = 100;
        existing.stock = 50;
        existing.creationAt = ZonedDateTime.now();

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "BU-1";
        newWarehouse.location = "LOC-1";
        newWarehouse.capacity = 150; // Increased capacity
        newWarehouse.stock = 50; // Matches existing

        Location location = new Location("LOC-1", 2, 300);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);
        when(warehouseStore.sumCapacityByLocation("LOC-1")).thenReturn(200); // existing + others

        useCase.replace(newWarehouse);

        verify(warehouseStore).update(newWarehouse);
        assertEquals(existing.creationAt, newWarehouse.creationAt);
    }

    @Test
    public void testReplaceFailsIfNotFound() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "BU-1";

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);

        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));
    }

    @Test
    public void testReplaceFailsIfArchived() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "BU-1";
        existing.archivedAt = ZonedDateTime.now();

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "BU-1";

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);

        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));
    }

    @Test
    public void testReplaceFailsIfStockMismatch() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "BU-1";
        existing.location = "LOC-1";
        existing.capacity = 100;
        existing.stock = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "BU-1";
        newWarehouse.location = "LOC-1";
        newWarehouse.capacity = 150;
        newWarehouse.stock = 40; // Mismatch

        Location location = new Location("LOC-1", 2, 300);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);

        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));
    }

    @Test
    public void testReplaceFailsIfCapacityLowerThanStock() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "BU-1";
        existing.location = "LOC-1";
        existing.capacity = 100;
        existing.stock = 50;

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "BU-1";
        newWarehouse.location = "LOC-1";
        newWarehouse.capacity = 40; // Too low
        newWarehouse.stock = 50;

        Location location = new Location("LOC-1", 2, 300);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);

        assertThrows(WarehouseValidationException.class, () -> useCase.replace(newWarehouse));
    }
}
