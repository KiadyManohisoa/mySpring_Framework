package mapping;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

public class MySession {

    HashMap<String, Object> keyValues;
    boolean makeInvalid = false;

    public void print() {
        for (Map.Entry<String, Object> entry : this.getKeyValues().entrySet()) {
            System.out.println("key " + entry.getKey() + " value " + entry.getValue());
        }
    }

    public void clearHttpSession(HttpSession session) {
        Enumeration<String> sessionKeys = session.getAttributeNames();
        while (sessionKeys.hasMoreElements()) {
            String key = sessionKeys.nextElement();
            session.removeAttribute(key);
        }
    }

    public void updateHttpSession(HttpSession session) {
        this.clearHttpSession(session);
        if (this.makeInvalid) {
            session.invalidate();
            return;
        }
        for (Map.Entry<String, Object> entry : this.getKeyValues().entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void add(String key, Object value) {
        this.getKeyValues().put(key, value);
    }

    public Object get(String key) {
        return this.getKeyValues().get(key);
    }

    public void put(String key, Object value) {
        this.getKeyValues().replace(key, value);
    }

    public void unset(String key) {
        this.getKeyValues().remove(key);
    }

    public void destroy() {
        this.makeInvalid = true;
        this.getKeyValues().clear();

    }

    public void setKeyValues(HttpSession session) {
        this.keyValues = new HashMap<String, Object>();
        Enumeration<String> sessionKeys = session.getAttributeNames();
        while (sessionKeys.hasMoreElements()) {
            String key = sessionKeys.nextElement();
            this.getKeyValues().put(key, session.getAttribute(key));
        }
    }

    public MySession() {
    }

    public HashMap<String, Object> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(HashMap<String, Object> keyValues) {
        this.keyValues = keyValues;
    }

}
