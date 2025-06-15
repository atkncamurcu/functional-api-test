package com.reqres.api.tests;

import com.reqres.api.models.LoginRequest;
import com.reqres.api.models.RegisterRequest;
import com.reqres.api.models.responses.ErrorResponse;
import com.reqres.api.models.responses.LoginResponse;
import com.reqres.api.models.responses.RegisterResponse;
import com.reqres.api.utils.PerformanceValidator;
import com.reqres.api.utils.ResponseHandler;
import com.reqres.api.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Test class for authentication flows (login, register, logout)
 */
public class AuthenticationTests extends BaseTest {
    
    // Note: We're no longer using token authentication, only API key
    
    @Test(priority = 1)
    public void testSuccessfulRegistration() {
        logger.info("Testing successful registration");
        
        RegisterRequest request = new RegisterRequest("eve.holt@reqres.in", "pistol");
        
        Response response = givenWithApiKey()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("register-response.json"))
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        RegisterResponse registerResponse = ResponseHandler.getRegister(response);
        
        Assert.assertNotNull(registerResponse.getId(), "ID should be present in response");
        Assert.assertNotNull(registerResponse.getToken(), "Token should be present in response");
        logger.info("Registration successful with token: {}", registerResponse.getToken());
    }
    
    @Test(priority = 2)
    public void testSuccessfulLogin() {
        logger.info("Testing successful login");
        
        LoginRequest request = new LoginRequest("eve.holt@reqres.in", "cityslicka");
        
        Response response = givenWithApiKey()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body(SchemaValidator.matchesSchema("login-response.json"))
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the response using our DTO/model class with ResponseHandler
        LoginResponse loginResponse = ResponseHandler.getLogin(response);
        
        Assert.assertNotNull(loginResponse.getToken(), "Token should be present in response");
        logger.info("Login successful with token: {}", loginResponse.getToken());
    }
    
    @Test(priority = 3)
    public void testProtectedResource() {
        logger.info("Testing access to protected resource with API key");
        
        Response response = givenWithApiKey()
                .when()
                .get("/users/2")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
    }
    
    @Test(priority = 4)
    public void testInvalidLogin() {
        logger.info("Testing login with invalid credentials");
        
        LoginRequest request = new LoginRequest("peter@klaven", "");
        
        Response response = givenWithApiKey()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the error response using our DTO/model class with ResponseHandler
        ErrorResponse errorResponse = ResponseHandler.getError(response);
        
        Assert.assertNotNull(errorResponse.getError(), "Error message should be present");
        logger.info("Invalid login error message: {}", errorResponse.getError());
    }
    
    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][] {
            {"", "password", "Missing email"},
            {"invalid-email", "password", "Invalid email format"},
            {"eve.holt@reqres.in", "", "Missing password"}
        };
    }
    
    @Test(priority = 5, dataProvider = "invalidCredentials")
    public void testLoginValidations(String email, String password, String expectedError) {
        logger.info("Testing login validation with: email={}, password={}", email, password);
        
        Map<String, String> requestBody = new HashMap<>();
        if (email != null) requestBody.put("email", email);
        if (password != null) requestBody.put("password", password);
        
        Response response = givenWithApiKey()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Parse the error response using our DTO/model class with ResponseHandler
        ErrorResponse errorResponse = ResponseHandler.getError(response);
        
        Assert.assertNotNull(errorResponse.getError(), "Error message should be present");
        logger.info("Login validation error message: {}", errorResponse.getError());
    }
    
    // testTokenTimeout test removed because the ReqRes API does not actually require API keys
    // and doesn't properly simulate token timeout behavior
}
