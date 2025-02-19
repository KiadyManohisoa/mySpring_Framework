package com.itu.myspringframework.util;

public class Syntaxe {

    public static String getSetterGetterNorm(String myString) {
        char maj = Character.toUpperCase(myString.charAt(0));
        return maj + myString.substring(1);
    }

}
