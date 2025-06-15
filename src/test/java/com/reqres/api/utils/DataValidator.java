package com.reqres.api.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

public class DataValidator {
    private static final Logger logger = LogManager.getLogger(DataValidator.class);

    public static void validatePageData(Map<String, Object> response, int expectedPage, int expectedPerPage) {
        logger.info("Validating page data with expected page: {} and perPage: {}", expectedPage, expectedPerPage);
        
        Assert.assertEquals(response.get("page"), expectedPage, "Page number doesn't match expected value");
        Assert.assertEquals(response.get("per_page"), expectedPerPage, "Per page value doesn't match expected value");
        
        int totalPages = (int) response.get("total_pages");
        int total = (int) response.get("total");
        int calculatedPages = (int) Math.ceil((double) total / expectedPerPage);
        
        Assert.assertEquals(totalPages, calculatedPages, 
                "Total pages should equal ceil(total/per_page)");
        
        List<?> data = (List<?>) response.get("data");
        Assert.assertTrue(data.size() <= expectedPerPage,
                "Number of items should not exceed per_page value");
    }
    
    public static void validateConsistentUserFields(List<Map<String, Object>> users) {
        logger.info("Validating consistent user fields in response");
        
        for (Map<String, Object> user : users) {
            Assert.assertNotNull(user.get("id"), "User ID must not be null");
            Assert.assertNotNull(user.get("email"), "User email must not be null");
            Assert.assertNotNull(user.get("first_name"), "User first name must not be null");
            Assert.assertNotNull(user.get("last_name"), "User last name must not be null");
            Assert.assertNotNull(user.get("avatar"), "User avatar must not be null");
            
            String email = (String) user.get("email");
            Assert.assertTrue(email.matches(".+@.+\\..+"), "Email should be in valid format");
            
            String avatar = (String) user.get("avatar");
            Assert.assertTrue(avatar.startsWith("https://"), "Avatar should be a valid HTTPS URL");
        }
    }
    
    public static void validateConsistentResourceFields(List<Map<String, Object>> resources) {
        logger.info("Validating consistent resource fields in response");
        
        for (Map<String, Object> resource : resources) {
            Assert.assertNotNull(resource.get("id"), "Resource ID must not be null");
            Assert.assertNotNull(resource.get("name"), "Resource name must not be null");
            Assert.assertNotNull(resource.get("year"), "Resource year must not be null");
            Assert.assertNotNull(resource.get("color"), "Resource color must not be null");
            Assert.assertNotNull(resource.get("pantone_value"), "Resource pantone_value must not be null");
            
            String color = (String) resource.get("color");
            Assert.assertTrue(color.matches("^#([A-Fa-f0-9]{6})$") || 
                              color.matches("^[A-Za-z]+$"), "Color should be a color name or hex code");
        }
    }
}
