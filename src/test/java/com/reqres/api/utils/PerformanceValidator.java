package com.reqres.api.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.concurrent.TimeUnit;

import io.restassured.response.Response;

public class PerformanceValidator {
    private static final Logger logger = LogManager.getLogger(PerformanceValidator.class);
    
    // Default threshold for response time in milliseconds
    private static final long DEFAULT_RESPONSE_TIME_THRESHOLD = 2000;
    
    public static void validateResponseTime(Response response) {
        validateResponseTime(response, DEFAULT_RESPONSE_TIME_THRESHOLD);
    }
    
    public static void validateResponseTime(Response response, long thresholdMillis) {
        long responseTime = response.timeIn(TimeUnit.MILLISECONDS);
        logger.info("Response time: {} ms with threshold: {} ms", responseTime, thresholdMillis);
        
        Assert.assertTrue(responseTime <= thresholdMillis, 
                "Response time (" + responseTime + " ms) exceeds threshold of " + thresholdMillis + " ms");
    }
}
