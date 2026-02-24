package com.fulfilment.application.monolith.stores;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("stores")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  private final StoreRepository storeRepository;
  private final LegacyStoreManagerGateway legacyStoreManagerGateway;
  private final TransactionManager transactionManager;

  public StoreResource(
      StoreRepository storeRepository,
      LegacyStoreManagerGateway legacyStoreManagerGateway,
      TransactionManager transactionManager) {
    this.storeRepository = storeRepository;
    this.legacyStoreManagerGateway = legacyStoreManagerGateway;
    this.transactionManager = transactionManager;
  }

  /**
   * Registers a callback to notify the legacy system only after the current
   * transaction has committed,
   * so the legacy system receives confirmed data.
   */
  private void notifyLegacyAfterCommit(Runnable action) {
    try {
      transactionManager
          .getTransaction()
          .registerSynchronization(
              new Synchronization() {
                @Override
                public void beforeCompletion() {
                  // No action required before completion
                }

                @Override
                public void afterCompletion(int status) {
                  if (status == Status.STATUS_COMMITTED) {
                    action.run();
                  }
                }
              });
    } catch (Exception e) {
      throw new IllegalStateException("Failed to register post-commit sync", e);
    }
  }

  @GET
  public List<Store> get() {
    return storeRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = storeRepository.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }
    if (store.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    storeRepository.persist(store);

    notifyLegacyAfterCommit(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = storeRepository.findById(id);

    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.setQuantityProductsInStock(updatedStore.getQuantityProductsInStock());

    Store entityRef = entity;
    notifyLegacyAfterCommit(
        () -> legacyStoreManagerGateway.updateStoreOnLegacySystem(entityRef));

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = storeRepository.findById(id);

    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.getQuantityProductsInStock() != 0) {
      entity.setQuantityProductsInStock(updatedStore.getQuantityProductsInStock());
    }

    Store entityRef = entity;
    notifyLegacyAfterCommit(
        () -> legacyStoreManagerGateway.updateStoreOnLegacySystem(entityRef));

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = storeRepository.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    storeRepository.delete(entity);
    return Response.status(204).build();
  }

}
