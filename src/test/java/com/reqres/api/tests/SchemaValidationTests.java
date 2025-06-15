package com.reqres.api.tests;

import com.reqres.api.models.LoginRequest;
import com.reqres.api.models.RegisterRequest;
import com.reqres.api.models.UserRequest;
import com.reqres.api.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * Test class for API response schema validation
 */
public class SchemaValidationTests extends BaseTest {
    
    @Test
    public void testUserListSchema() {
        logger.info("Testing schema validation for user list endpoint");
        
        givenWithApiKey()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("user-list.json"));
    }
    
    @Test
    public void testSingleUserSchema() {
        logger.info("Testing schema validation for single user endpoint");
        
        givenWithApiKey()
                .pathParam("id", 2)
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("single-user.json"));
    }
    
    @Test
    public void testResourceListSchema() {
        logger.info("Testing schema validation for resource list endpoint");
        
        givenWithApiKey()
                .when()
                .get("/unknown")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("resource-list.json"));
    }
    
    @Test
    public void testSingleResourceSchema() {
        logger.info("Testing schema validation for single resource endpoint");
        
        given()
                .pathParam("id", 2)
                .when()
                .get("/unknown/{id}")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("single-resource.json"));
    }
    
    @Test
    public void testLoginSchema() {
        logger.info("Testing schema validation for login endpoint");
        
        LoginRequest request = new LoginRequest("eve.holt@reqres.in", "cityslicka");
        
        given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("login-response.json"));
    }
    
    @Test
    public void testRegisterSchema() {
        logger.info("Testing schema validation for register endpoint");
        
        RegisterRequest request = new RegisterRequest("eve.holt@reqres.in", "pistol");
        
        given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("register-response.json"));
    }
    
    @Test
    public void testCreateUserSchema() {
        logger.info("Testing schema validation for create user response");
        
        UserRequest newUser = new UserRequest("morpheus", "leader");
        
        Response response = given()
                .body(newUser)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().response();
        
        // For create operation, verify expected fields are present
        response.then()
                .assertThat()
                .rootPath("")
                .body("name", org.hamcrest.Matchers.equalTo("morpheus"))
                .body("job", org.hamcrest.Matchers.equalTo("leader"))
                .body("id", org.hamcrest.Matchers.notNullValue())
                .body("createdAt", org.hamcrest.Matchers.notNullValue());
    }
    
    @Test
    public void testUpdateUserSchema() {
        logger.info("Testing schema validation for update user response");
        
        UserRequest updatedUser = new UserRequest("morpheus", "zion resident");
        
        Response response = given()
                .body(updatedUser)
                .pathParam("id", 2)
                .when()
                .put("/users/{id}")
                .then()
                .statusCode(200)
                .extract().response();
        
        // For update operation, verify expected fields are present
        response.then()
                .assertThat()
                .rootPath("")
                .body("name", org.hamcrest.Matchers.equalTo("morpheus"))
                .body("job", org.hamcrest.Matchers.equalTo("zion resident"))
                .body("updatedAt", org.hamcrest.Matchers.notNullValue());
    }
    
    @Test
    public void testPaginationSchema() {
        logger.info("Testing schema validation for pagination parameters");
        
        // Test with custom pagination parameters
        given()
                .queryParam("page", 2)
                .queryParam("per_page", 3)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("user-list.json"))
                .body("page", org.hamcrest.Matchers.equalTo(2))
                .body("per_page", org.hamcrest.Matchers.equalTo(3))
                .body("data", org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.lessThanOrEqualTo(3)));
    }
}
