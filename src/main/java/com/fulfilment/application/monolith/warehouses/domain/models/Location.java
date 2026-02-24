package com.fulfilment.application.monolith.warehouses.domain.models;

public class Location {
  private String identification;

  // maximum number of warehouses that can be created in this location
  private int maxNumberOfWarehouses;

  // maximum capacity of the location summing all the warehouse capacities
  private int maxCapacity;

  public Location(String identification, int maxNumberOfWarehouses, int maxCapacity) {
    this.identification = identification;
    this.maxNumberOfWarehouses = maxNumberOfWarehouses;
    this.maxCapacity = maxCapacity;
  }

  public String getIdentification() {
    return identification;
  }

  public int getMaxNumberOfWarehouses() {
    return maxNumberOfWarehouses;
  }

  public int getMaxCapacity() {
    return maxCapacity;
  }
}
