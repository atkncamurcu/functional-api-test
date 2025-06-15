package com.reqres.api.tests;

import com.reqres.api.models.Resource;
import com.reqres.api.models.User;
import com.reqres.api.models.responses.ResourceListResponse;
import com.reqres.api.models.responses.UserListResponse;
import com.reqres.api.utils.DataValidator;
import com.reqres.api.utils.PerformanceValidator;
import com.reqres.api.utils.ResponseHandler;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Test class for API pagination functionality
 */
public class PaginationTests extends BaseTest {
    
    @DataProvider(name = "paginationData")
    public Object[][] paginationData() {
        return new Object[][] {
            {"/users", 1, 6},       // Default page, default per_page
            {"/users", 2, 6},       // Next page, default per_page
            {"/users", 1, 3},       // Custom per_page
            {"/users", 2, 3},       // Custom per_page and page
            {"/unknown", 1, 6},     // Resource list, default page and per_page
            {"/unknown", 2, 3}      // Resource list, custom page and per_page
        };
    }
    
    @Test(dataProvider = "paginationData")
    public void testPagination(String endpoint, int page, int perPage) {
        logger.info("Testing pagination for endpoint: {} with page: {} and perPage: {}", endpoint, page, perPage);
        
        Response response = givenWithApiKey()
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .when()
                .get(endpoint)
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        PerformanceValidator.validateResponseTime(response);
        
        // Use ResponseHandler based on endpoint type
        if (endpoint.equals("/users")) {
            UserListResponse userListResponse = ResponseHandler.getUserList(response);
            
            // Validate pagination data
            Assert.assertEquals(userListResponse.getPage().intValue(), page, "Page number should match requested page");
            Assert.assertEquals(userListResponse.getPer_page().intValue(), perPage, "Per page should match requested perPage");
            Assert.assertTrue(userListResponse.getTotal() > 0, "Total should be positive");
            Assert.assertTrue(userListResponse.getTotal_pages() > 0, "Total pages should be positive");
            
            List<User> users = userListResponse.getData();
            Assert.assertNotNull(users, "User list should not be null");
            
            logger.info("Received {} users for page {} with per_page {}", users.size(), page, perPage);
            int expectedSize = Math.min(perPage, users.size());
            Assert.assertTrue(users.size() <= expectedSize, "Users list size should not exceed perPage");
            
        } else if (endpoint.equals("/unknown")) {
            ResourceListResponse resourceListResponse = ResponseHandler.getResourceList(response);
            
            // Validate pagination data
            Assert.assertEquals(resourceListResponse.getPage().intValue(), page, "Page number should match requested page");
            Assert.assertEquals(resourceListResponse.getPer_page().intValue(), perPage, "Per page should match requested perPage");
            Assert.assertTrue(resourceListResponse.getTotal() > 0, "Total should be positive");
            Assert.assertTrue(resourceListResponse.getTotal_pages() > 0, "Total pages should be positive");
            
            List<Resource> resources = resourceListResponse.getData();
            Assert.assertNotNull(resources, "Resource list should not be null");
            
            logger.info("Received {} resources for page {} with per_page {}", resources.size(), page, perPage);
            int expectedSize = Math.min(perPage, resources.size());
            Assert.assertTrue(resources.size() <= expectedSize, "Resource list size should not exceed perPage");
        }
        
        // Verify proper page boundaries
        if (page > 1) {
            // For page > 1, there should be previous data
            Response previousPageResponse = givenWithApiKey()
                    .queryParam("page", page - 1)
                    .queryParam("per_page", perPage)
                    .when()
                    .get(endpoint)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();
            
            if (endpoint.equals("/users")) {
                UserListResponse prevUserListResponse = ResponseHandler.getUserList(previousPageResponse);
                Assert.assertFalse(prevUserListResponse.getData().isEmpty(), "Previous page data should not be empty");
            } else if (endpoint.equals("/unknown")) {
                ResourceListResponse prevResourceListResponse = ResponseHandler.getResourceList(previousPageResponse);
                Assert.assertFalse(prevResourceListResponse.getData().isEmpty(), "Previous page data should not be empty");
            }
        }
        
        // Get total pages from response
        int totalPages;
        if (endpoint.equals("/users")) {
            UserListResponse userListResponse = ResponseHandler.getUserList(response);
            totalPages = userListResponse.getTotal_pages();
        } else {
            ResourceListResponse resourceListResponse = ResponseHandler.getResourceList(response);
            totalPages = resourceListResponse.getTotal_pages();
        }
        
        // If we're not on the last page, check that there is a next page with data
        if (page < totalPages) {
            Response nextPageResponse = givenWithApiKey()
                    .queryParam("page", page + 1)
                    .queryParam("per_page", perPage)
                    .when()
                    .get(endpoint)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();
            
            if (endpoint.equals("/users")) {
                UserListResponse nextUserListResponse = ResponseHandler.getUserList(nextPageResponse);
                List<User> nextUsers = nextUserListResponse.getData();
                
                // Last page might have fewer items
                if (page + 1 < totalPages) {
                    Assert.assertEquals(nextUsers.size(), perPage, 
                            "Next page should have perPage number of items unless it's the last page");
                } else {
                    Assert.assertTrue(nextUsers.size() <= perPage, 
                            "Last page should have perPage or fewer items");
                }
            } else if (endpoint.equals("/unknown")) {
                ResourceListResponse nextResourceListResponse = ResponseHandler.getResourceList(nextPageResponse);
                List<Resource> nextResources = nextResourceListResponse.getData();
                
                // Last page might have fewer items
                if (page + 1 < totalPages) {
                    Assert.assertEquals(nextResources.size(), perPage, 
                            "Next page should have perPage number of items unless it's the last page");
                } else {
                    Assert.assertTrue(nextResources.size() <= perPage, 
                            "Last page should have perPage or fewer items");
                }
            }
        }
    }
    
    @Test
    public void testPaginationBeyondAvailableData() {
        logger.info("Testing pagination beyond available data");
        
        // Get maximum page number
        Response response = givenWithApiKey()
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        UserListResponse userListResponse = ResponseHandler.getUserList(response);
        int totalPages = userListResponse.getTotal_pages();
        int invalidPage = totalPages + 10; // Definitely beyond available data
        
        logger.info("Requesting invalid page number: {} (total pages: {})", invalidPage, totalPages);
        
        Response invalidPageResponse = givenWithApiKey()
                .queryParam("page", invalidPage)
                .when()
                .get("/users")
                .then()
                .statusCode(200) // ReqRes returns 200 even for invalid pages
                .extract()
                .response();
        
        UserListResponse invalidPageUserListResponse = ResponseHandler.getUserList(invalidPageResponse);
        List<User> users = invalidPageUserListResponse.getData();
        Assert.assertTrue(users.isEmpty(), "Data should be empty for page beyond total pages");
    }
    
    @Test
    public void testPaginationLimits() {
        logger.info("Testing pagination limits and edge cases");
        
        // Test with page=0 (should default to page 1)
        Response response1 = givenWithApiKey()
                .queryParam("page", 0)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        UserListResponse userListResponse1 = ResponseHandler.getUserList(response1);
        Assert.assertEquals(userListResponse1.getPage().intValue(), 1, "Page should default to 1");
        
        // Test with negative page (should default to page 1)
        Response response2 = givenWithApiKey()
                .queryParam("page", -1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        UserListResponse userListResponse2 = ResponseHandler.getUserList(response2);
        Assert.assertEquals(userListResponse2.getPage().intValue(), 1, "Page should default to 1");
        
        // Test with very large page number (should return empty data array)
        Response response3 = givenWithApiKey()
                .queryParam("page", 1000)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        UserListResponse userListResponse3 = ResponseHandler.getUserList(response3);
        Assert.assertTrue(userListResponse3.getData().isEmpty(), "Data should be empty for very large page number");
        
        // Test with very large per_page
        Response response4 = givenWithApiKey()
                .queryParam("per_page", 1000)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        UserListResponse userListResponse4 = ResponseHandler.getUserList(response4);
        List<User> users = userListResponse4.getData();
        Assert.assertTrue(users.size() <= 1000, "Data size should be less than or equal to 1000 for very large per_page");
    }
}
