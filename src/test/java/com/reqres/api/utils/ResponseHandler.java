package com.reqres.api.utils;

import com.reqres.api.models.responses.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for handling API responses and converting them to model objects
 */
public class ResponseHandler {
    private static final Logger logger = LogManager.getLogger(ResponseHandler.class);

    /**
     * Parse a SingleUserResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return SingleUserResponse model
     */
    public static SingleUserResponse getSingleUser(Response response) {
        logger.info("Parsing SingleUserResponse");
        return response.as(SingleUserResponse.class);
    }
    
    /**
     * Parse a UserListResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return UserListResponse model
     */
    public static UserListResponse getUserList(Response response) {
        logger.info("Parsing UserListResponse");
        return response.as(UserListResponse.class);
    }
    
    /**
     * Parse a SingleResourceResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return SingleResourceResponse model
     */
    public static SingleResourceResponse getSingleResource(Response response) {
        logger.info("Parsing SingleResourceResponse");
        return response.as(SingleResourceResponse.class);
    }
    
    /**
     * Parse a ResourceListResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return ResourceListResponse model
     */
    public static ResourceListResponse getResourceList(Response response) {
        logger.info("Parsing ResourceListResponse");
        return response.as(ResourceListResponse.class);
    }
    
    /**
     * Parse a LoginResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return LoginResponse model
     */
    public static LoginResponse getLogin(Response response) {
        logger.info("Parsing LoginResponse");
        return response.as(LoginResponse.class);
    }
    
    /**
     * Parse a RegisterResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return RegisterResponse model
     */
    public static RegisterResponse getRegister(Response response) {
        logger.info("Parsing RegisterResponse");
        return response.as(RegisterResponse.class);
    }
    
    /**
     * Parse a UserCreateResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return UserCreateResponse model
     */
    public static UserCreateResponse getUserCreate(Response response) {
        logger.info("Parsing UserCreateResponse");
        return response.as(UserCreateResponse.class);
    }
    
    /**
     * Parse a UserUpdateResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return UserUpdateResponse model
     */
    public static UserUpdateResponse getUserUpdate(Response response) {
        logger.info("Parsing UserUpdateResponse");
        return response.as(UserUpdateResponse.class);
    }
    
    /**
     * Parse an ErrorResponse from the API response
     * 
     * @param response The REST-assured response object
     * @return ErrorResponse model
     */
    public static ErrorResponse getError(Response response) {
        logger.info("Parsing ErrorResponse");
        return response.as(ErrorResponse.class);
    }
}
