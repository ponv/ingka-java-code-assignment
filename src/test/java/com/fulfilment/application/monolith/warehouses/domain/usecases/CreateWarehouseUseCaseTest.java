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

class CreateWarehouseUseCaseTest {

    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private WarehouseValidator validator;

    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateWarehouseUseCase(warehouseStore, validator);
    }

    @Test
    void testCreateSuccess() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-1");
        warehouse.setLocation("LOC-1");
        warehouse.setCapacity(100);
        warehouse.setStock(50);

        when(validator.validateLocation("LOC-1")).thenReturn(new Location("LOC-1", 1, 100));

        useCase.create(warehouse);

        verify(validator).validateBusinessUnitCodeUnique("BU-1");
        verify(validator).validateLocation("LOC-1");
        verify(validator).validateLocationFeasibility(any(), eq("LOC-1"));
        verify(validator).validateCapacity(any(), eq("LOC-1"), eq(100), isNull());
        verify(warehouseStore).create(warehouse);
        assertNotNull(warehouse.getCreationAt());
    }

    @Test
    void testCreateFailsIfCapacityLowerThanStock() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU-1");
        warehouse.setLocation("LOC-1");
        warehouse.setCapacity(40);
        warehouse.setStock(50);

        when(validator.validateLocation("LOC-1")).thenReturn(new Location("LOC-1", 1, 100));

        assertThrows(WarehouseValidationException.class, () -> useCase.create(warehouse));
        verify(warehouseStore, never()).create(any());
    }
}
