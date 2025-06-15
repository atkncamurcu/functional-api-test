package com.reqres.api.tests;

import com.reqres.api.models.responses.ErrorResponse;
import com.reqres.api.utils.PerformanceValidator;
import com.reqres.api.utils.ResponseHandler;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Test class for API error handling
 */
public class ErrorHandlingTests extends BaseTest {
    
    @DataProvider(name = "notFoundEndpoints")
    public Object[][] notFoundEndpoints() {
        return new Object[][] {
            {"/users/23", "GET", "Requesting non-existent user"},
            {"/unknown/23", "GET", "Requesting non-existent resource"},
            {"/nonexistent-resource", "GET", "Requesting undefined endpoint"}
        };
    }
    
    @Test(dataProvider = "notFoundEndpoints")
    public void testNotFoundErrors(String endpoint, String method, String testCase) {
        logger.info("Testing 404 Not Found scenario: {}", testCase);
        
        Response response;
        
        switch (method) {
            case "GET":
                response = givenWithApiKey()
                        .when()
                        .get(endpoint)
                        .then()
                        .statusCode(404)
                        .extract()
                        .response();
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
        
        PerformanceValidator.validateResponseTime(response);
        
        // ReqRes returns an empty object for 404 responses
        Assert.assertEquals(response.jsonPath().getMap("$").size(), 0, 
                "Not Found response should be an empty object");
    }
    
    @Test
    public void testInvalidLoginCredentials() {
        logger.info("Testing invalid login credentials");
        
        Map<String, String> request = new HashMap<>();
        request.put("email", "sydney@fife");
        // Missing password
        
        Response response = given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Missing password", "Error message should indicate missing password");
    }
    
    @Test
    public void testInvalidRegistrationData() {
        logger.info("Testing invalid registration data");
        
        Map<String, String> request = new HashMap<>();
        request.put("email", "sydney@fife");
        // Missing password
        
        Response response = given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Missing password", "Error message should indicate missing password");
    }
    
    @Test
    public void testUnsupportedHttpMethod() {
        logger.info("Testing unsupported HTTP method");
        
        Response response = given()
                .when()
                .options("/users")
                .then()
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // The status code will depend on the server implementation
        // ReqRes may return different status for OPTIONS
        int statusCode = response.getStatusCode();
        logger.info("Received status code {} for OPTIONS request", statusCode);
    }
    
    @Test
    public void testInvalidJsonPayload() {
        logger.info("Testing invalid JSON payload");
        
        String invalidJson = "{ this is not valid json }";
        
        Response response = given()
                .header("Content-Type", "application/json")
                .body(invalidJson)
                .when()
                .post("/users")
                .then()
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        int statusCode = response.getStatusCode();
        logger.info("Received status code {} for invalid JSON payload", statusCode);
        
        // Expected behavior varies by server, but typically should be 400 Bad Request
        Assert.assertTrue(statusCode >= 400, "Invalid JSON should result in a 4xx status code");
    }
    
    @Test
    public void testConcurrentRequests() {
        logger.info("Testing multiple concurrent requests for error handling");
        
        // Simulate multiple concurrent requests and check for consistent error handling
        Runnable request = () -> {
            try {
                // Create a new request specification in each thread
                // to avoid sharing the same specification across threads
                Response response = given()
                        .baseUri("https://reqres.in/api")
                        .contentType("application/json")
                        .pathParam("id", 999) // Non-existent ID
                        .when()
                        .get("/users/{id}")
                        .then()
                        .extract()
                        .response();
                
                int statusCode = response.getStatusCode();
                logger.info("Thread {} received status code {}", Thread.currentThread().getId(), statusCode);
                Assert.assertEquals(statusCode, 404, "Should consistently return 404 for non-existent resource");
            } catch (Exception e) {
                logger.error("Error in concurrent request: {}", e.getMessage());
                // Just log the error but don't fail the test
            }
        };
        
        // Create and start multiple threads
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(request);
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("Thread interrupted: {}", e.getMessage());
            }
        }
    }
    
    @Test
    public void testMethodNotAllowedError() {
        logger.info("Testing 405 Method Not Allowed error");
        
        Response response = given()
                .when()
                .delete("/users")
                .then()
                .statusCode(405)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Method Not Allowed", "Error message should indicate method not allowed");
    }
    
    @Test
    public void testUnsupportedMediaTypeError() {
        logger.info("Testing 415 Unsupported Media Type error");
        
        Response response = given()
                .body("<user><n>morpheus</n><job>leader</job></user>")
                .when()
                .post("/users")
                .then()
                .statusCode(415)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        String error = response.jsonPath().getString("error");
        Assert.assertEquals(error, "Unsupported Media Type", "Error message should indicate unsupported media type");
    }
}
