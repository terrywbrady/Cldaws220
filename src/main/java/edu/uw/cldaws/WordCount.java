package edu.uw.cldaws;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WordCount {

    private WordCountParser wcParser = new WordCountParser();
    private WordCountCache wcCache = new WordCountCache();
    private WordCountQueue wcQueue = new WordCountQueue();

    public static void main(String[] args) throws IOException {
        //WordCount wc = new WordCount();
        long start = new Date().getTime();
        long lastReport = new Date().getTime();
        System.out.println(returnJsonMessage(200,"foo"));
    }
    
    public String myWebHandler(LinkedHashMap map, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + map.toString());
        String url = map.containsKey("url") ? map.get("url").toString() : "";
        return checkUrl(url);
    }

    public WordCount() {
    }
   
    public String checkUrl(String url) throws IOException {
        if (url == null) {
            return returnJsonMessage(500, "'url' parameter must be provided");
        }
        if (url.isEmpty()) {
            return returnJsonMessage(500, "'url' parameter must be provided");
        }
        String result = wcCache.checkCacheVal(url);
        
        if (result == null) {
            if (wcQueue.queueRequest(url)) {
                return returnJsonMessage(200, "Not in cache. Request queued. Try again later.");
            } else {
                return returnJsonMessage(200, "Not in cache. Request could not be queued.");
            }
        }
        return returnJsonMessage(200, result);
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
    
    
    public Void handleRequest(SQSEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();

        ArrayList<WordCountMessage> mlist = new ArrayList<>();
        for(SQSMessage msg : event.getRecords()){
            WordCountMessage wcm = new WordCountMessage(msg);
            logger.log(String.format("%s -- %s", wcm.getUrl(), wcm.getReceipt()));
            mlist.add(wcm);
        }
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
                result = wcParser.getCountAsJson(url);
                wcCache.putCacheVal(url, result);
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
        String body;
        
        LambdaResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
            headers.put("Content-Type", "application/json");
        }
    }
    
    public static String returnJsonMessage(int status, String s) {
        return new Gson().toJson(new LambdaResponse(status, s));
    }
    
}
