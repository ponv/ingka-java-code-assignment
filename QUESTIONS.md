# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would consider refactoring toward a single, consistent strategy.

Today we have three patterns:
(1) Store uses the active-record style (PanacheEntity with static methods like listAll(), persist() on the entity);
(2) Product uses a repository (PanacheRepository<Product>) but the entity is still the same object used in the API;
(3) Warehouse uses a ports-and-adapters style with a domain model (Warehouse), a persistence model (DbWarehouse), and a repository implementing a WarehouseStore port.

I would not force everything into the heaviest style (Warehouse). Instead, I would standardise on the repository-over-entity approach for simple CRUD: a dedicated repository per aggregate, entities as JPA models, and resources calling the repository instead of the entity. That would mean refactoring Store to use a StoreRepository (like Product) and having the resource depend on the repository.

Benefits: one place for persistence logic, easier to test and swap implementations, and no static methods on entities. For domains with richer rules (like Warehouse), keeping a separate domain model and mapping in the adapter is still the right choice.

So: unify Store and Product around the repository pattern; keep the Warehouse-style separation where the domain justifies it.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI-first (Warehouse): 
Pros:
 — contract is explicit and shareable; 
 - generated types and interface reduce drift and boilerplate; API versioning and evolution can be discussed in one place. 
Cons:
 — generated code can be rigid; you need to map between generated DTOs and domain/entities; 
 - small spec changes require regeneration and sometimes manual fixes.

Hand-coded (Product, Store): 
Pros:
 — full control and simplicity; no mapping layer if the resource uses the entity/DTO directly; quick to change. 
Cons:
 — contract lives only in code and annotations; docs and client SDKs need to be maintained or generated separately; easier to introduce inconsistencies (e.g. status codes, error shape).

Choice: 
 - I would use OpenAPI-first for any API that is (or will be) consumed by multiple clients or teams, or that needs a clear, versioned contract (e.g. Warehouse, future public or partner APIs). 
 - I would keep or introduce hand-coded endpoints for small, internal-only or prototype APIs. For this codebase, moving Product and Store to an OpenAPI spec would be a reasonable next step if we want consistent documentation and client generation across all resources; otherwise, keeping them hand-coded is acceptable as long as we document the decision and any contract in code or a small markdown/example.```