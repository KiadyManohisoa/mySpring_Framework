package mapping;

import jakarta.servlet.http.HttpSession;

public class MySession {

    private HttpSession session;

    public void destroy() {
        this.getSession().invalidate();
    }

    public void unset(String key) {
        this.getSession().removeAttribute(key);
    }

    public void put(String key, Object value) {
        this.getSession().setAttribute(key, value);
    }

    public Object get(String key) {
        return this.getSession().getAttribute(key);
    }

    public void add(String key, Object value) {
        this.getSession().setAttribute(key, value);
    }

    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

}
