package com.blakersfield.gameagentsystem.utility;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
public class ObjectMapperProvider {
    private static ObjectMapper objectMapper = null;
    public static ObjectMapper getObjectMapper(){
        if (objectMapper == null){ //super permissive objectmapper to hopefully spare some headache
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            objectMapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
            objectMapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            objectMapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        }
        return objectMapper;
    }
}
