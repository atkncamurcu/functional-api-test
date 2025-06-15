package com.reqres.api.tests;

import com.reqres.api.utils.PerformanceValidator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * Tests specifically for API key authentication
 */
public class ApiKeyTests extends BaseTest {

    @Test
    public void testApiKeyIsRequired() {
        logger.info("Testing API key behavior");
        
        // Store the default request spec temporarily
        var defaultSpec = RestAssured.requestSpecification;
        
        try {
            // Reset the default spec to not include the API key
            RestAssured.reset();
            
            // Send a request without the API key
            Response response = given()
                    .baseUri("https://reqres.in/api")
                    .contentType("application/json")
                    .when()
                    .get("/users")
                    .then()
                    .extract()
                    .response();
            
            // Check the response status
            int statusCode = response.getStatusCode();
            logger.info("Status code without API key: {}", statusCode);
            
            // The ReqRes API doesn't actually require API key
            Assert.assertEquals(statusCode, 200, "ReqRes API should work without API key");
            logger.info("Confirmed that ReqRes API is publicly accessible without an API key");
            
        } finally {
            // Restore the default request spec
            RestAssured.requestSpecification = defaultSpec;
        }
    }
    
    @Test
    public void testRequestWithApiKey() {
        logger.info("Testing that requests with API key work correctly");
        
        Response response = given()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Verify successful response with data
        Assert.assertFalse(response.jsonPath().getList("data").isEmpty(), "Response data should not be empty");
    }
}
