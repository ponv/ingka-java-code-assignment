package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseResourceTest {

    @Test
    void testListAllWarehouses() {
        given()
                .when()
                .get("/warehouse")
                .then()
                .statusCode(200)
                .body("id", hasItem("MWH.001"))
                .body("id", hasItem("MWH.012"));
    }

    @Test
    void testGetWarehouseById() {
        // Testing a warehouse that is likely not modified by other tests
        given()
                .when()
                .get("/warehouse/MWH.012")
                .then()
                .statusCode(200)
                .body("id", is("MWH.012"))
                .body("location", is("AMSTERDAM-001"))
                .body("capacity", is(50))
                .body("stock", is(5));
    }

    @Test
    void testGetNonExistentWarehouse() {
        given()
                .when()
                .get("/warehouse/NONEXISTENT")
                .then()
                .statusCode(404);
    }

    @Test
    void testCreateWarehouse() {
        com.warehouse.api.beans.Warehouse newWarehouse = new com.warehouse.api.beans.Warehouse();
        newWarehouse.setId("MWH.W_NEW_" + System.currentTimeMillis());
        newWarehouse.setLocation("AMSTERDAM-001");
        newWarehouse.setCapacity(40);
        newWarehouse.setStock(0);

        given()
                .contentType(ContentType.JSON)
                .body(newWarehouse)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(200)
                .body("id", is(newWarehouse.getId()))
                .body("location", is("AMSTERDAM-001"));
    }

    @Test
    void testCreateWarehouseMissingId() {
        com.warehouse.api.beans.Warehouse newWarehouse = new com.warehouse.api.beans.Warehouse();
        newWarehouse.setLocation("AMSTERDAM-001");

        given()
                .contentType(ContentType.JSON)
                .body(newWarehouse)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(422);
    }

    @Test
    void testArchiveWarehouse() {
        // We'll create one then archive it to be safe
        com.warehouse.api.beans.Warehouse toArchive = new com.warehouse.api.beans.Warehouse();
        toArchive.setId("MWH.TO_ARCHIVE_" + System.currentTimeMillis());
        toArchive.setLocation("AMSTERDAM-001");
        toArchive.setCapacity(10);
        toArchive.setStock(0);

        given()
                .contentType(ContentType.JSON)
                .body(toArchive)
                .when()
                .post("/warehouse")
                .then()
                .statusCode(200);

        // Archive it
        given()
                .when()
                .delete("/warehouse/" + toArchive.getId())
                .then()
                .statusCode(204);

        // Verify it's gone from active list
        given()
                .when()
                .get("/warehouse")
                .then()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.not(hasItem(toArchive.getId())));
    }
}
