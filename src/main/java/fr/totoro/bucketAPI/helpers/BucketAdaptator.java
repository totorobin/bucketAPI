package fr.totoro.bucketAPI.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class BucketAdaptator {

    public static DBObject toDBObject(JsonNode body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        BasicDBObject result = mapper.readValue(body.toString(), BasicDBObject.class);
        return result;

    }

    public static JsonNode toJsonNode(DBObject document) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(JSON.serialize(document));
    }
}
