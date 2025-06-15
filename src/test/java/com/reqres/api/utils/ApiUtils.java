package com.reqres.api.utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiUtils {
    private static final Logger logger = LogManager.getLogger(ApiUtils.class);
    private static final String BASE_URL = "https://reqres.in/api";
    private static final String API_KEY = "reqres-free-v1";
    
    public static RequestSpecification getRequestSpec() {
        logger.info("Creating base request specification");
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }
    
    public static RequestSpecification getRequestSpecWithAuth() {
        logger.info("Creating request specification with API key auth");
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .addHeader("x-api-key", API_KEY)
                .log(LogDetail.ALL)
                .build();
    }
    
    public static ResponseSpecification getResponseSpec() {
        logger.info("Creating response specification");
        return new ResponseSpecBuilder()
                .log(LogDetail.ALL)
                .build();
    }
    
    public static void setupRestAssured() {
        logger.info("Setting up RestAssured with API key authentication");
        RestAssured.requestSpecification = getRequestSpecWithAuth();
        RestAssured.responseSpecification = getResponseSpec();
    }
    
    /**
     * Adds API key authentication header to an existing request specification
     * 
     * @param requestSpec The existing request specification
     * @return Request specification with API key authentication header added
     */
    public static RequestSpecification addApiKeyAuth(RequestSpecification requestSpec) {
        logger.info("Adding API key authentication to request");
        return requestSpec.header("x-api-key", API_KEY);
    }
}
