package com.itu.myspringframework.mapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModelView {

    String url;
    HashMap<String, Object> data = new HashMap<String, Object>();

    public ModelView() {
    }

    public ModelView(String url) {
        this.setUrl(url);
    }

    public void add(String key, Object value) {
        this.getData().put(key, value);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public String generateParameters() {
        if (data.isEmpty()) {
            return "";
        }
        StringBuilder queryString = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            queryString.append(encodedKey).append("=").append(encodedValue).append("&");
        }
        queryString.deleteCharAt(queryString.length() - 1); 
        return queryString.toString();
    }

}
