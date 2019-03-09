package edu.uw.cldaws;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class WordCountParser {
    //private static int MAX = 10_000;
    private static int COUNT = 10;
    private TreeMap<String,Integer> map = new TreeMap<>();
    
    public WordCountParser() {
        setString("");
    }
    public void setUrl(String s) throws IOException {
        if (s == null) {
            System.err.println("Url not found");
            setString("");
            return;
        }
        URL url = new URL(s);
        try(InputStream is = url.openStream()) {
            try(Scanner scan = new Scanner(is)){
                parseVals(scan);
            }
        }
    }

    //Week 1 - allow count param, disregard as we test caching
    public String getCountAsJson(String url) throws IOException {
        setUrl(url);
        Collection<String> results = getTopKeys(COUNT);
        String result = new Gson().toJson(results);
        return result;
    }

    
    
    public void setString(String s) {
        try(Scanner scan = new Scanner(s)){
            parseVals(scan);
        }
    }

    private void parseVals(Scanner scan) {
        map.clear();
        scan.useDelimiter(Pattern.compile("[^\\w']"));
        while(scan.hasNext()) {
            String key = scan.next().toLowerCase();
            if (key.isEmpty()) {
                continue;
            }
            Integer val = map.containsKey(key) ? map.get(key) + 1: 1;
            map.put(key, val);
        }        
    }
    
    public Collection<String> getTopKeys(int n) {
        return map.keySet()
            .stream()
            .sorted((o1, o2)->map.get(o2).compareTo(map.get(o1)))
            .limit(n)
            .collect(Collectors.toList());
    }
}
