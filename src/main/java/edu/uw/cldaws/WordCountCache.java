package edu.uw.cldaws;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class WordCountCache {
    private static String RESULT = "result";
    private static String CACHE="WordCountCache";
    private static String URL = "url";
    public String checkCacheVal(String url) {
        String result;
        HashMap<String,AttributeValue> key_to_get = new HashMap<>();
        
        key_to_get.put(URL, AttributeValue.builder().s(url).build());

        GetItemRequest request = GetItemRequest.builder()
            .tableName(CACHE)
            .key(key_to_get)
            .build();

        try(DynamoDbClient ddb = DynamoDbClient.create()) {
            Map<String,AttributeValue> returned_item = ddb.getItem(request).item();
            result = returned_item.containsKey(RESULT) ? returned_item.get(RESULT).s() : null;
        }
        return result;
    }

    public void putCacheVal(String url, String result) {
        HashMap<String,AttributeValue> map = new HashMap<>();
        map.put(URL, AttributeValue.builder().s(url).build());
        map.put(RESULT, AttributeValue.builder().s(result).build());
        
        PutItemRequest req = PutItemRequest.builder()
            .tableName(CACHE)
            .item(map)
            .build();
        try(DynamoDbClient ddb = DynamoDbClient.create()) {
            ddb.putItem(req);
        }
    }

}
