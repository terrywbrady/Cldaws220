package edu.uw.cldaws;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WordCount {

    public enum ReturnType {
        SUCCESS(200),
        INVALID_URL(500),
        INVALID_INPUT(500),
        BAD_RESULT(500);
        
        int status;
        ReturnType(int status) {
            this.status = status;
        }
    }
    private WordCountParser wcParser = new WordCountParser();
    private WordCountCache wcCache = new WordCountCache();
    private WordCountQueue wcQueue = new WordCountQueue();

    public static void main(String[] args) throws IOException {
        //WordCount wc = new WordCount();
        long start = new Date().getTime();
        long lastReport = new Date().getTime();
        System.out.println(returnJsonMessage(ReturnType.SUCCESS,"foo"));
    }
    
    public LambdaResponse myWebHandler(LinkedHashMap map, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + map.toString());
        logger.log("context : " + context.toString());
        String url = "";
        if (map.containsKey("url")) {
            url = map.get("url").toString();
        } else if (map.containsKey("queryStringParameters")) {
            Map qsp = (Map)map.get("queryStringParameters");
            if (qsp.containsKey("url")) {
                url = qsp.get("url").toString();
            }
        }
        logger.log("url : " + url);
        LambdaResponse resp = checkUrl(url);
        String respstr = resp.asJsonString();
        logger.log("resp: "+respstr);
        return resp;
    }

    public WordCount() {
    }
   
    public LambdaResponse checkUrl(String url) throws IOException {
        if (url == null) {
            return returnJsonMessage(ReturnType.INVALID_INPUT, "'url' parameter must be provided");
        }
        if (url.isEmpty()) {
            return returnJsonMessage(ReturnType.INVALID_INPUT, "'url' parameter cannot be empty");
        }
        
        try {
            URL urlobj = new URL(url);
        } catch (MalformedURLException e) {
            return returnJsonMessage(ReturnType.INVALID_URL, e.getMessage());
        }
        
        String result = wcCache.checkCacheVal(url);
        
        if (result == null) {
            if (wcQueue.queueRequest(url)) {
                return returnJsonMessage(ReturnType.SUCCESS, "Not in cache. Request queued. Try again later.");
            } else {
                return returnJsonMessage(ReturnType.SUCCESS, "Not in cache. Request could not be queued.");
            }
        }
        JsonParser parser = new JsonParser();
        System.out.println(result);
        try {
            JsonArray o = parser.parse(result).getAsJsonArray();
            return returnJsonMessage(ReturnType.SUCCESS, o);
        } catch (Exception e) {
            return returnJsonMessage(ReturnType.BAD_RESULT, result);
        }
    }

    public long reportStatus(long start, long lastReport) {
        long now = new Date().getTime();
        long diff = now - lastReport;
        if (diff > 60_000) {
            long min = (now - start)/60_000;
            System.out.println(String.format("System has run for %d min", min));
            return now;
        }
        return lastReport;
    }
    
    
    public Void handleRequest(SQSEvent event, Context context) throws IOException
    {
        LambdaLogger logger = context.getLogger();

        ArrayList<WordCountMessage> mlist = new ArrayList<>();
        for(SQSMessage msg : event.getRecords()){
            WordCountMessage wcm = new WordCountMessage(msg);
            logger.log(String.format("%s -- %s", wcm.getUrl(), wcm.getReceipt()));
            mlist.add(wcm);
        }
        processMessageList(mlist);
        return null;
    }
    

    public boolean processQueue() throws IOException {
        return processMessageList(wcQueue.getMessage());
    }
    
    public boolean processMessageList(List<WordCountMessage> mlist) throws IOException {
        if (mlist.isEmpty()) {
            return false;
        } else {
            WordCountMessage m = mlist.get(0);
            String url  = m.getUrl();
            String result = wcCache.checkCacheVal(url);
            if (result == null) {
                System.out.println("Processing " + url);
                try {
                    result = wcParser.getCountAsJson(url);
                    wcCache.putCacheVal(url, result);
                } catch (Exception e) {
                    System.out.println("INVALID_URL: " + e.getMessage());
                }
            } else {
                System.out.println("Already processed" + url);
            }
            wcQueue.removeMessage(m);
        }
        return true;
    }
   
    
    public static class LambdaResponse {
        int statusCode;
        Map<String,String> headers = new HashMap<>();
        Object body;
        private Gson g = new GsonBuilder().create();

        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return g.toJson(body);
        }
        
        LambdaResponse(int statusCode, Object body) {
            this.statusCode = statusCode;
            this.body = body;
            headers.put("Content-Type", "application/json");
        }
        
        public String asJsonString() {
            return g.toJson(body);
        }
    }
    
    public static LambdaResponse returnJsonMessage(ReturnType rt, Object s) {
        if (rt == ReturnType.SUCCESS) {
            return new LambdaResponse(rt.status, s);
        } else {
            return new LambdaResponse(rt.status, String.format("%s: %s", rt.name(), s));
        }
    }
}
