package edu.uw.cldaws;

import java.io.IOException;

import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WordCount {

    private WordCountParser wcParser = new WordCountParser();
    private WordCountCache wcCache = new WordCountCache();
    private WordCountQueue wcQueue = new WordCountQueue();

    public static void main(String[] args) {
        WordCount wc = new WordCount();
        long start = new Date().getTime();
        long lastReport = new Date().getTime();
        try {
            while(true) {
                wc.processQueue();
                TimeUnit.SECONDS.sleep(1);
                lastReport = wc.reportStatus(start, lastReport);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public WordCount() {
    }
   
    public String checkUrl(String url) throws IOException {
        if (url == null) {
            return returnJsonMessage("'url' parameter must be provided");
        }
        if (url.isEmpty()) {
            return returnJsonMessage("'url' parameter must be provided");
        }
        String result = wcCache.checkCacheVal(url);
        
        if (result == null) {
            if (wcQueue.queueRequest(url)) {
                return returnJsonMessage("Not in cache. Request queued. Try again later.");
            } else {
                return returnJsonMessage("Not in cache. Request could not be queued.");
            }
        }
        return result;
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
    
    public boolean processQueue() throws IOException {
        List<WordCountMessage> mlist = wcQueue.getMessage();
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
   
    public static String returnJsonMessage(String s) {
        HashMap<String,String> map = new HashMap<>();
        map.put("message", s);
        return new Gson().toJson(map);
    }
    
}
