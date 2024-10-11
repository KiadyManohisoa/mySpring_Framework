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
        boolean answer = false; // initialement non equals
        if (this.getVerb() == toAdd.getVerb()) {
            answer = true;
        }
        if (this.getMethod() == toAdd.getMethod()) {
            answer = true;
        }
        return answer;
    }

    public VerbMethod(Method method) {
        this.setMethod(method);
        this.setVerb();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getVerb(), this.getMethod());
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
