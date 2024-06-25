package util;

public class Syntaxe {

    public static String getSetterNorm(String myString) {
        char maj = Character.toUpperCase(myString.charAt(0));
        return maj + myString.substring(1);
    }

}
