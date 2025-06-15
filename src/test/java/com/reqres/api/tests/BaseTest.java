package com.reqres.api.tests;

import com.reqres.api.utils.ApiUtils;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static io.restassured.RestAssured.given;

/**
 * Base class for all test classes. Sets up REST Assured configuration.
 */
public abstract class BaseTest {
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);

    @BeforeClass
    public void setUp() {
        logger.info("Setting up test class with API key authentication...");
        RestAssured.defaultParser = Parser.JSON;
        ApiUtils.setupRestAssured();
    }
    
    @BeforeMethod
    public void logTestMethod() {
        logger.info("Starting test method: {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }
    
    /**
     * Helper method to get a request specification with API key authentication
     * 
     * @return RequestSpecification with API key authentication
     */
    protected RequestSpecification givenWithApiKey() {
        // API key is already set in the default request spec via ApiUtils.setupRestAssured()
        // This method ensures that all test methods use a consistent approach
        return given();
    }
}
