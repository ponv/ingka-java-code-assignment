package com.fulfilment.application.monolith.warehouses.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WarehouseValidatorTest {

    @Mock
    private WarehouseStore warehouseStore;
    @Mock
    private LocationResolver locationResolver;

    private WarehouseValidator validator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        validator = new WarehouseValidator(warehouseStore, locationResolver);
    }

    @Test
    void testValidateLocationSuccess() {
        Location loc = new Location("LOC-1", 1, 100);
        when(locationResolver.resolveByIdentifier("LOC-1")).thenReturn(loc);

        Location result = validator.validateLocation("LOC-1");
        assertEquals(loc, result);
    }

  @Test
  void testValidateLocationNotFound() {
    when(locationResolver.resolveByIdentifier("UNKNOWN")).thenReturn(null);
    assertThrows(WarehouseValidationException.class, () -> validator.validateLocation("UNKNOWN"));
  }

  @Test
  void testValidateBusinessUnitCodeUniqueSuccess() {
    when(warehouseStore.findByBusinessUnitCode("NEW-BU")).thenReturn(null);
    assertDoesNotThrow(() -> validator.validateBusinessUnitCodeUnique("NEW-BU"));
  }

  @Test
  void testValidateBusinessUnitCodeUniqueFails() {
    when(warehouseStore.findByBusinessUnitCode("OLD-BU")).thenReturn(new Warehouse());
    assertThrows(WarehouseValidationException.class, () -> validator.validateBusinessUnitCodeUnique("OLD-BU"));
  }

    @Test
    void testValidateLocationFeasibilitySuccess() {
        Location loc = new Location("LOC-1", 2, 100);
        when(warehouseStore.countActiveByLocation("LOC-1")).thenReturn(1L);
        assertDoesNotThrow(() -> validator.validateLocationFeasibility(loc, "LOC-1"));
    }

    @Test
    void testValidateLocationFeasibilityFails() {
        Location loc = new Location("LOC-1", 1, 100);
        when(warehouseStore.countActiveByLocation("LOC-1")).thenReturn(1L);
        assertThrows(WarehouseValidationException.class, () -> validator.validateLocationFeasibility(loc, "LOC-1"));
    }

    @Test
    void testValidateCapacitySuccess() {
        Location loc = new Location("LOC-1", 2, 200);
        when(warehouseStore.sumCapacityByLocation("LOC-1")).thenReturn(150);
        // 150 - 50 (existing) + 80 (new) = 180 <= 200
        assertDoesNotThrow(() -> validator.validateCapacity(loc, "LOC-1", 80, 50));
    }

    @Test
    void testValidateCapacityFails() {
        Location loc = new Location("LOC-1", 2, 200);
        when(warehouseStore.sumCapacityByLocation("LOC-1")).thenReturn(150);
        // 150 - 0 + 60 = 210 > 200
        assertThrows(WarehouseValidationException.class, () -> validator.validateCapacity(loc, "LOC-1", 60, null));
    }
}
