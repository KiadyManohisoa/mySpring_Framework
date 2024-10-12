package mapping;

import java.lang.reflect.*;
import java.util.Objects;
import annotation.Post;

public class VerbMethod {

    String verb;
    Method method;

    @Override
    public boolean equals(Object o) {
        VerbMethod toAdd = (VerbMethod) o;
        if (this.getVerb().compareTo(toAdd.getVerb()) == 0) {
            return true;
        }
        if (this.getMethod().getName().equals(toAdd.getMethod().getName())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash("NONE");
    }

    public VerbMethod(Method method) {
        this.setMethod(method);
        this.setVerb();
    }

    public String getVerb() {
        return verb;
    }

    void setVerb() {
        this.setVerb("GET"); // default http method
        if (this.getMethod().isAnnotationPresent(Post.class)) {
            this.setVerb("POST");
        }
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
