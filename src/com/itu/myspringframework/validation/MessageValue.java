package com.itu.myspringframework.validation;

public class MessageValue {

    String message;
    String value;

    public MessageValue() {
        this.setMessage(new String());
        this.setValue(new String());
    }

    public MessageValue(String message, String value) {
        this.setMessage(message);
        this.setValue(value);
    }

    public MessageValue(String value) {
        this.setMessage(new String());
        this.setValue(value);
    }

    public String getMessage() {
        return "<p style=\"color:red;\">" + message + "</p>";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
