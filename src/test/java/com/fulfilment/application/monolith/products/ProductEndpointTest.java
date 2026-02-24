package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductEndpointTest {

    @Test
    void testCrudProduct() {
        final String path = "product";

        // List all
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body("id", not(java.util.Collections.emptyList()));

        // Create new product
        String uniqueName = "MALM_" + System.currentTimeMillis();
        Product newProduct = new Product(uniqueName);
        newProduct.setStock(20);

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(newProduct)
                .when()
                .post(path)
                .then()
                .statusCode(201)
                .body("name", is(uniqueName))
                .extract().path("id");

        // Get single product
        given()
                .when()
                .get(path + "/" + id)
                .then()
                .statusCode(200)
                .body("name", is(uniqueName));

        // Update product
        Product updatedProduct = new Product(uniqueName + "-UPDATED");
        updatedProduct.setStock(15);
        given()
                .contentType(ContentType.JSON)
                .body(updatedProduct)
                .when()
                .put(path + "/" + id)
                .then()
                .statusCode(200)
                .body("name", is(uniqueName + "-UPDATED"));

        // Delete
        given().when().delete(path + "/" + id).then().statusCode(204);

        // Verify it's gone
        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body("name", not(hasItem(uniqueName + "-UPDATED")));
    }

    @Test
    void testGetNonExistentProduct() {
        given()
                .when()
                .get("product/999")
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateNonExistentProduct() {
        Product product = new Product("TEST");
        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .put("product/999")
                .then()
                .statusCode(404);
    }

    @Test
    void testCreateInvalidProduct() {
        Product product = new Product();
        // id set manually should fail 422
        product.id = 123L;
        given()
                .contentType(ContentType.JSON)
                .body(product)
                .when()
                .post("product")
                .then()
                .statusCode(422);
    }
}
