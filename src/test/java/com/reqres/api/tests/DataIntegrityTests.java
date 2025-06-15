package com.reqres.api.tests;

import com.reqres.api.models.User;
import com.reqres.api.models.UserRequest;
import com.reqres.api.utils.DataValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Test class for data integrity validation
 */
public class DataIntegrityTests extends BaseTest {
    
    @Test
    public void testUserDataIntegrity() {
        logger.info("Testing user data integrity across API endpoints");
        
        // Get a list of users
        Response listResponse = givenWithApiKey()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        List<Map<String, Object>> users = listResponse.jsonPath().getList("data");
        Assert.assertFalse(users.isEmpty(), "User list should not be empty");
        
        // Validate data consistency across all users
        DataValidator.validateConsistentUserFields(users);
        
        // Check individual user data integrity
        for (Map<String, Object> userMap : users) {
            int userId = ((Integer) userMap.get("id")).intValue();
            
            Response singleResponse = givenWithApiKey()
                    .pathParam("id", userId)
                    .when()
                    .get("/users/{id}")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();
            
            Map<String, Object> singleUser = singleResponse.jsonPath().getMap("data");
            
            // Verify data consistency between list and single user endpoint
            Assert.assertEquals(singleUser.get("id"), userMap.get("id"), 
                    "User ID should match between list and single user endpoint");
            Assert.assertEquals(singleUser.get("email"), userMap.get("email"), 
                    "User email should match between list and single user endpoint");
            Assert.assertEquals(singleUser.get("first_name"), userMap.get("first_name"), 
                    "User first name should match between list and single user endpoint");
            Assert.assertEquals(singleUser.get("last_name"), userMap.get("last_name"), 
                    "User last name should match between list and single user endpoint");
        }
    }
    
    @Test
    public void testResourceDataIntegrity() {
        logger.info("Testing resource data integrity across API endpoints");
        
        // Get a list of resources
        Response listResponse = given()
                .when()
                .get("/unknown")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        List<Map<String, Object>> resources = listResponse.jsonPath().getList("data");
        Assert.assertFalse(resources.isEmpty(), "Resource list should not be empty");
        
        // Validate data consistency across all resources
        DataValidator.validateConsistentResourceFields(resources);
        
        // Check individual resource data integrity
        for (Map<String, Object> resourceMap : resources) {
            int resourceId = ((Integer) resourceMap.get("id")).intValue();
            
            Response singleResponse = given()
                    .pathParam("id", resourceId)
                    .when()
                    .get("/unknown/{id}")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();
            
            Map<String, Object> singleResource = singleResponse.jsonPath().getMap("data");
            
            // Verify data consistency between list and single resource endpoint
            Assert.assertEquals(singleResource.get("id"), resourceMap.get("id"), 
                    "Resource ID should match between list and single resource endpoint");
            Assert.assertEquals(singleResource.get("name"), resourceMap.get("name"), 
                    "Resource name should match between list and single resource endpoint");
            Assert.assertEquals(singleResource.get("year"), resourceMap.get("year"), 
                    "Resource year should match between list and single resource endpoint");
            Assert.assertEquals(singleResource.get("color"), resourceMap.get("color"), 
                    "Resource color should match between list and single resource endpoint");
            Assert.assertEquals(singleResource.get("pantone_value"), resourceMap.get("pantone_value"), 
                    "Resource pantone value should match between list and single resource endpoint");
        }
    }
    
    @Test
    public void testCreateUpdateUserDataIntegrity() {
        logger.info("Testing data integrity for create and update user operations");
        
        // Create a new user
        UserRequest newUser = new UserRequest("John Test", "QA Engineer");
        
        Response createResponse = given()
                .body(newUser)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .response();
        
        String userId = createResponse.jsonPath().getString("id");
        String name = createResponse.jsonPath().getString("name");
        String job = createResponse.jsonPath().getString("job");
        
        Assert.assertEquals(name, newUser.getName(), "Created user name should match request");
        Assert.assertEquals(job, newUser.getJob(), "Created user job should match request");
        
        // Update the user
        UserRequest updatedUser = new UserRequest("John Updated", "Senior QA");
        
        Response updateResponse = given()
                .body(updatedUser)
                .when()
                .put("/users/" + userId)
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        String updatedName = updateResponse.jsonPath().getString("name");
        String updatedJob = updateResponse.jsonPath().getString("job");
        
        Assert.assertEquals(updatedName, updatedUser.getName(), "Updated user name should match request");
        Assert.assertEquals(updatedJob, updatedUser.getJob(), "Updated user job should match request");
    }
    
    @Test
    public void testDataPersistenceAcrossRequests() {
        logger.info("Testing data persistence across multiple requests");
        
        // This is a simplified test as ReqRes doesn't actually persist changes
        // In a real API, we would verify that changes are retained
        
        // First, let's get an existing user
        Response getResponse = given()
                .pathParam("id", 1)
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        User originalUser = getResponse.jsonPath().getObject("data", User.class);
        
        // Now let's try to update this user
        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("name", originalUser.getFirst_name() + " " + originalUser.getLast_name() + " Updated");
        updateRequest.put("job", "Updated Position");
        
        given()
                .pathParam("id", 1)
                .body(updateRequest)
                .when()
                .put("/users/{id}")
                .then()
                .statusCode(200);
        
        // Now get the user again - in a real API, we'd expect to see our changes
        // ReqRes will still return the original user as it doesn't persist changes
        Response getAfterUpdateResponse = given()
                .pathParam("id", 1)
                .when()
                .get("/users/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        User retrievedUser = getAfterUpdateResponse.jsonPath().getObject("data", User.class);
        
        // In ReqRes, this will match as changes aren't persisted
        // In a real API test, we'd check if the update was actually applied
        Assert.assertEquals(retrievedUser.getId(), originalUser.getId(), 
                "User ID should remain the same after update");
        
        logger.info("Note: ReqRes API does not actually persist changes, so we can't verify update persistence.");
    }
    
    @Test
    public void testDataTypes() {
        logger.info("Testing data types in API responses");
        
        Response response = given()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        // Verify data types for pagination info
        Assert.assertTrue(response.jsonPath().get("page") instanceof Integer, "Page should be an integer");
        Assert.assertTrue(response.jsonPath().get("per_page") instanceof Integer, "Per page should be an integer");
        Assert.assertTrue(response.jsonPath().get("total") instanceof Integer, "Total should be an integer");
        Assert.assertTrue(response.jsonPath().get("total_pages") instanceof Integer, "Total pages should be an integer");
        
        // Verify data types for user objects
        List<Map<String, Object>> users = response.jsonPath().getList("data");
        for (Map<String, Object> user : users) {
            Assert.assertTrue(user.get("id") instanceof Integer, "User ID should be an integer");
            Assert.assertTrue(user.get("email") instanceof String, "User email should be a string");
            Assert.assertTrue(user.get("first_name") instanceof String, "User first name should be a string");
            Assert.assertTrue(user.get("last_name") instanceof String, "User last name should be a string");
            Assert.assertTrue(user.get("avatar") instanceof String, "User avatar should be a string");
        }
    }
}
