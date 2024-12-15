package validation;

import java.util.HashMap;

public class ValueController {

    HashMap<String, MessageValue> errorMap;
    boolean hasError = false;

    public MessageValue get(String key) {
        if (this.getErrorMap().containsKey(key)) {
            return this.getErrorMap().get(key);
        } else {
            return new MessageValue();
        }
    }

    public void add(String key, MessageValue mv) {
        this.getErrorMap().put(key, mv);
    }

    public ValueController() {
        this.errorMap = new HashMap<>();
    }

    public ValueController(HashMap<String, MessageValue> errorMap) {
        this.errorMap = errorMap;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public HashMap<String, MessageValue> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(HashMap<String, MessageValue> errorMap) {
        this.errorMap = errorMap;
    }

}
