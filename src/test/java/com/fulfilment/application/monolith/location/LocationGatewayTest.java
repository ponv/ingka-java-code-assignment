package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  @Test
  void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertEquals("ZWOLLE-001", location.getIdentification());
    assertEquals(1, location.getMaxNumberOfWarehouses());
    assertEquals(40, location.getMaxCapacity());
  }

  @Test
  void testWhenResolveNonExistingLocationShouldReturnNull() {
    LocationGateway locationGateway = new LocationGateway();
    assertNull(locationGateway.resolveByIdentifier("UNKNOWN-001"));
  }

  @Test
  void testWhenResolveNullIdentifierShouldReturnNull() {
    LocationGateway locationGateway = new LocationGateway();
    assertNull(locationGateway.resolveByIdentifier(null));
  }
}
