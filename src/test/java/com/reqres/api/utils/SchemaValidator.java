package com.reqres.api.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;

public class SchemaValidator {
    private static final Logger logger = LogManager.getLogger(SchemaValidator.class);
    private static final String SCHEMA_PATH = "schemas/";

    public static JsonSchemaValidator matchesSchema(String schemaFileName) {
        logger.info("Validating response against schema: {}", schemaFileName);
        URL schemaUrl = SchemaValidator.class.getClassLoader().getResource(SCHEMA_PATH + schemaFileName);
        if (schemaUrl == null) {
            throw new RuntimeException("Schema file not found: " + schemaFileName);
        }
        return JsonSchemaValidator.matchesJsonSchema(new File(schemaUrl.getFile()));
    }
}
