package com.fulfilment.application.monolith.warehouses.domain;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseValidator {

    private final WarehouseStore warehouseStore;
    private final LocationResolver locationResolver;

    public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
        this.warehouseStore = warehouseStore;
        this.locationResolver = locationResolver;
    }

    public Location validateLocation(String locationId) {
        Location loc = locationResolver.resolveByIdentifier(locationId);
        if (loc == null) {
            throw new WarehouseValidationException("Invalid or unknown location: " + locationId);
        }
        return loc;
    }

    public void validateBusinessUnitCodeUnique(String buCode) {
        if (warehouseStore.findByBusinessUnitCode(buCode) != null) {
            throw new WarehouseValidationException("Business unit code already exists: " + buCode);
        }
    }

    public void validateLocationFeasibility(Location loc, String locationId) {
        long currentCount = warehouseStore.countActiveByLocation(locationId);
        if (currentCount >= loc.getMaxNumberOfWarehouses()) {
            throw new WarehouseValidationException(
                    "Maximum number of warehouses (" + loc.getMaxNumberOfWarehouses()
                            + ") already reached for location: " + locationId);
        }
    }

    public void validateCapacity(Location loc, String locationId, int newCapacity, Integer existingCapacity) {
        int currentTotalCapacity = warehouseStore.sumCapacityByLocation(locationId);
        int capacityToSubtract = existingCapacity != null ? existingCapacity : 0;

        if (currentTotalCapacity - capacityToSubtract + newCapacity > loc.getMaxCapacity()) {
            throw new WarehouseValidationException(
                    "Total capacity would exceed location max capacity " + loc.getMaxCapacity()
                            + " for location: " + locationId);
        }
    }
}
