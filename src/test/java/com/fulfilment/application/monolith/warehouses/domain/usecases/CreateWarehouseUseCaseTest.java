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

public class CreateWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private LocationResolver locationResolver;

    private CreateWarehouseUseCase useCase;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
    }

    @Test
    public void testCreateSuccess() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU-1";
        warehouse.location = "LOC-1";
        warehouse.capacity = 100;
        warehouse.stock = 50;

        Location location = new Location("LOC-1", 2, 200);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);
        when(warehouseStore.countActiveByLocation("LOC-1")).thenReturn(1L);
        when(warehouseStore.sumCapacityByLocation("LOC-1")).thenReturn(50);

        useCase.create(warehouse);

        verify(warehouseStore).create(warehouse);
        assertNotNull(warehouse.creationAt);
    }

    @Test
    public void testCreateFailsIfCodeExists() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU-1";

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(new Warehouse());

        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
        verify(warehouseStore, never()).create(any());
    }

    @Test
    public void testCreateFailsIfLocationInvalid() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU-1";
        warehouse.location = "UNKNOWN";

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("UNKNOWN")).thenReturn(null);

        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    }

    @Test
    public void testCreateFailsIfMaxWarehousesReached() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU-1";
        warehouse.location = "LOC-1";

        Location location = new Location("LOC-1", 1, 200);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);
        when(warehouseStore.countActiveByLocation("LOC-1")).thenReturn(1L);

        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    }

    @Test
    public void testCreateFailsIfCapacityExceedsMax() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU-1";
        warehouse.location = "LOC-1";
        warehouse.capacity = 100;
        warehouse.stock = 50;

        Location location = new Location("LOC-1", 2, 150);

        when(warehouseStore.findByBusinessUnitCode("BU-1")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(location);
        when(warehouseStore.countActiveByLocation("LOC-1")).thenReturn(0L);
        when(warehouseStore.sumCapacityByLocation("LOC-1")).thenReturn(100);

        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
    }
}
