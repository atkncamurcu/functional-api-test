package com.reqres.api.tests;

import com.reqres.api.models.Resource;
import com.reqres.api.models.responses.ResourceListResponse;
import com.reqres.api.models.responses.SingleResourceResponse;
import com.reqres.api.models.responses.UserCreateResponse;
import com.reqres.api.models.responses.UserUpdateResponse;
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
 * Test class for Resource CRUD operations
 */
public class ResourceCrudTests extends BaseTest {
    
    private int resourceId;
    private static final String RESOURCE_ENDPOINT = "/unknown";
    
    @Test(priority = 1)
    public void testListResources() {
        logger.info("Testing GET {} endpoint", RESOURCE_ENDPOINT);
        
        Response response = givenWithApiKey()
                .when()
                .get(RESOURCE_ENDPOINT)
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("resource-list.json"))
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        ResourceListResponse resourceListResponse = ResponseHandler.getResourceList(response);
        
        // Extract a resource ID for later tests
        resourceId = resourceListResponse.getData().get(0).getId();
        logger.info("Extracted resource id: {}", resourceId);
        
        Assert.assertTrue(resourceId > 0, "Resource ID should be a positive integer");
        Assert.assertTrue(resourceListResponse.getTotal() > 0, "Total resources count should be positive");
    }
    
    @Test(priority = 2)
    public void testGetSingleResource() {
        logger.info("Testing GET {}/{} endpoint with ID: {}", RESOURCE_ENDPOINT, resourceId, resourceId);
        
        Response response = givenWithApiKey()
                .pathParam("id", resourceId)
                .when()
                .get(RESOURCE_ENDPOINT + "/{id}")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("single-resource.json"))
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        SingleResourceResponse resourceResponse = ResponseHandler.getSingleResource(response);
        Resource resource = resourceResponse.getData();
        
        Assert.assertEquals(resource.getId().intValue(), resourceId, "Resource ID should match the requested ID");
    }
    
    @Test(priority = 3)
    public void testCreateResource() {
        logger.info("Testing POST {} endpoint", RESOURCE_ENDPOINT);
        
        Map<String, Object> newResource = new HashMap<>();
        newResource.put("name", "cerulean blue");
        newResource.put("year", 2023);
        newResource.put("color", "#1A85FF");
        newResource.put("pantone_value", "19-4052");
        
        Response response = givenWithApiKey()
                .body(newResource)
                .when()
                .post(RESOURCE_ENDPOINT)
                .then()
                .statusCode(201)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using UserCreateResponse since ReqRes uses the same format
        UserCreateResponse createResponse = ResponseHandler.getUserCreate(response);
        
        Assert.assertNotNull(createResponse.getId(), "ID should be present in response");
        Assert.assertNotNull(createResponse.getCreatedAt(), "Creation timestamp should be present");
    }
    
    @Test(priority = 4)
    public void testUpdateResourceWithPut() {
        logger.info("Testing PUT {}/{} endpoint with ID: {}", RESOURCE_ENDPOINT, resourceId, resourceId);
        
        Map<String, Object> updatedResource = new HashMap<>();
        updatedResource.put("name", "updated blue");
        updatedResource.put("color", "#0066FF");
        updatedResource.put("year", 2024);
        
        Response response = givenWithApiKey()
                .pathParam("id", resourceId)
                .body(updatedResource)
                .when()
                .put(RESOURCE_ENDPOINT + "/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using UserUpdateResponse since ReqRes uses the same format
        UserUpdateResponse updateResponse = ResponseHandler.getUserUpdate(response);
        
        Assert.assertNotNull(updateResponse.getUpdatedAt(), "Update timestamp should be present");
    }
    
    @Test(priority = 5)
    public void testUpdateResourceWithPatch() {
        logger.info("Testing PATCH {}/{} endpoint with ID: {}", RESOURCE_ENDPOINT, resourceId, resourceId);
        
        Map<String, Object> partialUpdate = new HashMap<>();
        partialUpdate.put("color", "#2294ED");
        
        Response response = givenWithApiKey()
                .pathParam("id", resourceId)
                .body(partialUpdate)
                .when()
                .patch(RESOURCE_ENDPOINT + "/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using UserUpdateResponse since ReqRes uses the same format
        UserUpdateResponse updateResponse = ResponseHandler.getUserUpdate(response);
        
        Assert.assertNotNull(updateResponse.getUpdatedAt(), "Update timestamp should be present");
    }
    
    @Test(priority = 6)
    public void testDeleteResource() {
        logger.info("Testing DELETE {}/{} endpoint with ID: {}", RESOURCE_ENDPOINT, resourceId, resourceId);
        
        Response response = givenWithApiKey()
                .pathParam("id", resourceId)
                .when()
                .delete(RESOURCE_ENDPOINT + "/{id}")
                .then()
                .statusCode(204)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // 204 response means successful deletion with no content in the response body
        Assert.assertEquals(response.body().asString(), "", "Response body should be empty for DELETE");
    }
}
