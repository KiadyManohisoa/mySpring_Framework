package mapping;

import java.util.HashMap;

public class ModelView {

    String url;
    HashMap<String, Object> data = new HashMap<String, Object>();

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

}
