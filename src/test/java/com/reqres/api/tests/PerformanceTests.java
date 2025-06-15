package com.reqres.api.tests;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * Test class for API performance testing
 */
public class PerformanceTests extends BaseTest {
    
    private static final long SINGLE_REQUEST_THRESHOLD_MS = 1000; // 1 second
    private static final long LIST_REQUEST_THRESHOLD_MS = 1500;   // 1.5 seconds
    
    @DataProvider(name = "endpointsForPerformance")
    public Object[][] endpointsForPerformance() {
        return new Object[][] {
            {"/users/1", "GET", SINGLE_REQUEST_THRESHOLD_MS, "Single user request"},
            {"/users", "GET", LIST_REQUEST_THRESHOLD_MS, "User list request"},
            {"/unknown/1", "GET", SINGLE_REQUEST_THRESHOLD_MS, "Single resource request"},
            {"/unknown", "GET", LIST_REQUEST_THRESHOLD_MS, "Resource list request"},
            {"/register", "POST", SINGLE_REQUEST_THRESHOLD_MS, "Registration request"},
            {"/login", "POST", SINGLE_REQUEST_THRESHOLD_MS, "Login request"}
        };
    }
    
    @Test(dataProvider = "endpointsForPerformance")
    public void testResponseTime(String endpoint, String method, long threshold, String description) {
        logger.info("Testing response time for {}: {}", description, endpoint);
        
        Response response;
        
        switch (method) {
            case "GET":
                response = givenWithApiKey()
                        .when()
                        .get(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case "POST":
                // Example payload for POST requests
                String payload = method.equals("POST") && endpoint.equals("/register") ?
                        "{\"email\":\"eve.holt@reqres.in\",\"password\":\"pistol\"}" :
                        "{\"email\":\"eve.holt@reqres.in\",\"password\":\"cityslicka\"}";
                
                response = given()
                        .body(payload)
                        .when()
                        .post(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
        
        long responseTimeMs = response.timeIn(TimeUnit.MILLISECONDS);
        logger.info("Response time for {}: {} ms (threshold: {} ms)", description, responseTimeMs, threshold);
        
        Assert.assertTrue(responseTimeMs <= threshold, 
                "Response time for " + description + " should be less than " + threshold + " ms but was " + responseTimeMs + " ms");
    }
    
    @Test
    public void testMultipleConsecutiveRequests() {
        logger.info("Testing response time for multiple consecutive requests");
        
        long cumulativeTime = 0;
        int requestCount = 10;
        
        for (int i = 0; i < requestCount; i++) {
            logger.info("Executing request {} of {}", i + 1, requestCount);
            
            long startTime = System.currentTimeMillis();
            
            Response response = given()
                    .when()
                    .get("/users")
                    .then()
                    .extract()
                    .response();
            
            long endTime = System.currentTimeMillis();
            long requestTime = endTime - startTime;
            cumulativeTime += requestTime;
            
            logger.info("Request {} took {} ms", i + 1, requestTime);
        }
        
        double averageTime = (double) cumulativeTime / requestCount;
        logger.info("Average response time for {} consecutive requests: {} ms", requestCount, averageTime);
        
        Assert.assertTrue(averageTime <= LIST_REQUEST_THRESHOLD_MS, 
                "Average response time should be less than " + LIST_REQUEST_THRESHOLD_MS + " ms but was " + averageTime + " ms");
    }
    
    @Test
    public void testConcurrentRequests() {
        logger.info("Testing response time for concurrent requests");
        
        final int threadCount = 5;
        final List<Long> responseTimes = new ArrayList<>();
        
        // Create and start multiple threads
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                logger.info("Thread {} executing request", index);
                
                long startTime = System.currentTimeMillis();
                
                Response response = given()
                        .when()
                        .get("/users")
                        .then()
                        .extract()
                        .response();
                
                long endTime = System.currentTimeMillis();
                long requestTime = endTime - startTime;
                
                synchronized (responseTimes) {
                    responseTimes.add(requestTime);
                }
                
                logger.info("Thread {} request completed in {} ms", index, requestTime);
                
                Assert.assertTrue(response.getStatusCode() == 200, 
                        "Expected status code 200 but got " + response.getStatusCode());
            });
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
        
        // Calculate average response time
        double totalTime = 0;
        for (long time : responseTimes) {
            totalTime += time;
        }
        double averageTime = totalTime / responseTimes.size();
        
        logger.info("Average response time for {} concurrent requests: {} ms", threadCount, averageTime);
        
        // For concurrent requests, allow a slightly higher threshold
        long concurrentThreshold = LIST_REQUEST_THRESHOLD_MS * 2;
        Assert.assertTrue(averageTime <= concurrentThreshold, 
                "Average concurrent response time should be less than " + concurrentThreshold + 
                " ms but was " + averageTime + " ms");
    }
    
    @Test
    public void testSequentialRequests() {
        logger.info("Testing sequential requests performance");
        
        final int SEQUENTIAL_REQUESTS = 10;
        final String endpoint = "/users";
        
        long totalTime = 0;
        
        for (int i = 0; i < SEQUENTIAL_REQUESTS; i++) {
            long startTime = System.currentTimeMillis();
            
            given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(200);
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            totalTime += responseTime;
            
            logger.info("Request {} took {} ms", i + 1, responseTime);
        }
        
        double averageTime = (double) totalTime / SEQUENTIAL_REQUESTS;
        logger.info("Average response time for {} sequential requests: {} ms", SEQUENTIAL_REQUESTS, averageTime);
        
        Assert.assertTrue(averageTime <= LIST_REQUEST_THRESHOLD_MS, 
                "Average response time should be less than " + LIST_REQUEST_THRESHOLD_MS + " ms but was " + averageTime + " ms");
    }
}
