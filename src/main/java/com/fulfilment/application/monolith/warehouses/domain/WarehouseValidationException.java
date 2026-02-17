package com.fulfilment.application.monolith.warehouses.domain;

/**
 * Thrown when warehouse creation, replacement or archiving fails business validation.
 * Allows the REST layer to map to appropriate HTTP status (e.g. 400, 404, 409).
 */
public class WarehouseValidationException extends RuntimeException {

  public WarehouseValidationException(String message) {
    super(message);
  }

  public WarehouseValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
