package com.reqres.api.tests;

import com.reqres.api.models.User;
import com.reqres.api.models.UserRequest;
import com.reqres.api.models.responses.*;
import com.reqres.api.utils.PerformanceValidator;
import com.reqres.api.utils.ResponseHandler;
import com.reqres.api.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Test class for User CRUD operations
 */
public class UserCrudTests extends BaseTest {
    
    private int userId;
    
    @Test(priority = 1)
    public void testListUsers() {
        logger.info("Testing GET /users endpoint");
        
        Response response = givenWithApiKey()
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("user-list.json"))
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        UserListResponse userListResponse = ResponseHandler.getUserList(response);
        
        // Extract a userId for later tests
        userId = userListResponse.getData().get(0).getId();
        logger.info("Extracted user id: {}", userId);
        
        Assert.assertTrue(userId > 0, "User ID should be a positive integer");
        Assert.assertEquals(userListResponse.getPage().intValue(), 1, "Page number should match the requested page");
        Assert.assertTrue(userListResponse.getTotal() > 0, "Total users count should be positive");
    }
    
    @Test(priority = 2)
    public void testGetSingleUser() {
        logger.info("Testing GET /users/{} endpoint with ID: {}", userId, userId);
        
        Response response = givenWithApiKey()
                .pathParam("id", userId)
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("single-user.json"))
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        SingleUserResponse userResponse = ResponseHandler.getSingleUser(response);
        User user = userResponse.getData();
        
        Assert.assertEquals(user.getId().intValue(), userId, "User ID should match the requested ID");
    }
    
    @Test(priority = 3)
    public void testCreateUser() {
        logger.info("Testing POST /users endpoint");
        
        UserRequest newUser = new UserRequest("John", "Developer");
        
        Response response = givenWithApiKey()
                .body(newUser)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        UserCreateResponse createResponse = ResponseHandler.getUserCreate(response);
        
        Assert.assertNotNull(createResponse.getId(), "ID should be present in response");
        Assert.assertEquals(createResponse.getName(), newUser.getName(), "Name should match the request");
        Assert.assertEquals(createResponse.getJob(), newUser.getJob(), "Job should match the request");
        Assert.assertNotNull(createResponse.getCreatedAt(), "Creation timestamp should be present");
    }
    
    @Test(priority = 4)
    public void testUpdateUserWithPut() {
        logger.info("Testing PUT /users/{} endpoint with ID: {}", userId, userId);
        
        UserRequest updatedUser = new UserRequest("John Updated", "Senior Developer");
        
        Response response = givenWithApiKey()
                .pathParam("id", userId)
                .body(updatedUser)
                .when()
                .put("/users/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        UserUpdateResponse updateResponse = ResponseHandler.getUserUpdate(response);
        
        Assert.assertEquals(updateResponse.getName(), updatedUser.getName(), "Name should be updated");
        Assert.assertEquals(updateResponse.getJob(), updatedUser.getJob(), "Job should be updated");
        Assert.assertNotNull(updateResponse.getUpdatedAt(), "Update timestamp should be present");
    }
    
    @Test(priority = 5)
    public void testUpdateUserWithPatch() {
        logger.info("Testing PATCH /users/{} endpoint with ID: {}", userId, userId);
        
        Map<String, String> partialUpdate = new HashMap<>();
        partialUpdate.put("job", "Tech Lead");
        
        Response response = givenWithApiKey()
                .pathParam("id", userId)
                .body(partialUpdate)
                .when()
                .patch("/users/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        UserUpdateResponse updateResponse = ResponseHandler.getUserUpdate(response);
        
        Assert.assertEquals(updateResponse.getJob(), partialUpdate.get("job"), "Job should be updated");
        Assert.assertNotNull(updateResponse.getUpdatedAt(), "Update timestamp should be present");
    }
    
    @Test(priority = 6)
    public void testDeleteUser() {
        logger.info("Testing DELETE /users/{} endpoint with ID: {}", userId, userId);
        
        Response response = givenWithApiKey()
                .pathParam("id", userId)
                .when()
                .delete("/users/{id}")
                .then()
                .statusCode(204)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // 204 response means successful deletion with no content in the response body
        Assert.assertEquals(response.body().asString(), "", "Response body should be empty for DELETE");
    }
}
