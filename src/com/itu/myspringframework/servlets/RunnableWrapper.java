package com.itu.myspringframework.servlets;

public class RunnableWrapper {

    Runnable callback = () -> {};

    public Runnable getCallback() {
        return callback;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
}
