package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreEndpointTest {

        @Test
        void testListAllStores() {
                given()
                                .when()
                                .get("/stores")
                                .then()
                                .statusCode(200)
                                .body("id", not(java.util.Collections.emptyList()));
        }

        @Test
        void testGetSingleStore() {
                given()
                                .when()
                                .get("/stores/1")
                                .then()
                                .statusCode(200)
                                .body("id", is(1));
        }

        @Test
        void testGetNonExistentStore() {
                given()
                                .when()
                                .get("/stores/999")
                                .then()
                                .statusCode(404);
        }

        @Test
        void testCreateStore() {
                String uniqueName = "STORE_" + System.currentTimeMillis();
                Store store = new Store(uniqueName);
                store.setQuantityProductsInStock(15);

                given()
                                .contentType(ContentType.JSON)
                                .body(store)
                                .when()
                                .post("/stores")
                                .then()
                                .statusCode(201)
                                .body("name", is(uniqueName))
                                .body("quantityProductsInStock", is(15));
        }

        @Test
        void testCreateStoreInvalid() {
                Store store = new Store(null);
                given()
                                .contentType(ContentType.JSON)
                                .body(store)
                                .when()
                                .post("/stores")
                                .then()
                                .statusCode(422);
        }

        @Test
        void testUpdateStore() {
                Store updatedStore = new Store("HAARLEM-UPDATED");
                updatedStore.setQuantityProductsInStock(20);

                given()
                                .contentType(ContentType.JSON)
                                .body(updatedStore)
                                .when()
                                .put("/stores/1")
                                .then()
                                .statusCode(200)
                                .body("name", is("HAARLEM-UPDATED"));
        }

        @Test
        void testPatchStore() {
                Store patchStore = new Store("AMSTERDAM-PATCHED");
                patchStore.setQuantityProductsInStock(50);

                given()
                                .contentType(ContentType.JSON)
                                .body(patchStore)
                                .when()
                                .patch("/stores/2")
                                .then()
                                .statusCode(200)
                                .body("name", is("AMSTERDAM-PATCHED"))
                                .body("quantityProductsInStock", is(50));
        }

        @Test
        void testDeleteStore() {
                String toDeleteName = "TO_DELETE_" + System.currentTimeMillis();
                Store store = new Store(toDeleteName);
                Integer id = given()
                                .contentType(ContentType.JSON)
                                .body(store)
                                .when()
                                .post("/stores")
                                .then()
                                .statusCode(201)
                                .extract().path("id");

                given()
                                .when()
                                .delete("/stores/" + id)
                                .then()
                                .statusCode(204);

                given()
                                .when()
                                .get("/stores")
                                .then()
                                .statusCode(200)
                                .body("name", not(hasItem(toDeleteName)));
        }
}
