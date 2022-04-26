package model;

import java.util.Map;

public class HttpRequestContext {

    private static ThreadLocal<Map<String,String>> data = new ThreadLocal<>();

    public static Map<String,String> getData() { return (Map)data.get();}
}
