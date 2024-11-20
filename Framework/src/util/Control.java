package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import annotation.value_control.*;

public class Control {

    public void checkOnField(Field field, String inputValue) throws Exception {
        Util util = new Util();
        Annotation[] fAnnotations = field.getAnnotations();
        for (Annotation annotation : fAnnotations) {
            if (annotation.annotationType().equals(Required.class)) {
                if (inputValue == null || inputValue.isEmpty()) {
                    throw new Exception("The attribute " + field.getName() + " is required and has to be set");
                }
            }
            if (annotation.annotationType().equals(Numeric.class)) {
                if (!util.isNumeric(inputValue)) {
                    throw new Exception("The attribute " + field.getName() + " must have a numeric value");
                }
            }
        }
    }

    public Object getFieldValue(Field field, Object reference) throws Exception {
        Method mGetter = reference.getClass().getDeclaredMethod(
                "get" + Syntaxe.getSetterGetterNorm(field.getName()),
                new Class<?>[] {});
        return mGetter.invoke(reference, new Object[] {});
    }

    boolean hasDefaultValue(Field field, Object binded) throws Exception {
        Object value = new Convertor().getDefaultValue(field.getType());
        System.out.println("For the field " + field.getName() + " has the value " + this.getFieldValue(field, binded)
                + " and the default value " + value);
        if (value.equals(this.getFieldValue(field, binded))) {
            return true;
        }
        return false;
    }

    public void checkOnFields(Object binded) throws Exception {
        Field[] fields = binded.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            // case required
            if (fields[i].isAnnotationPresent(Required.class)) {
                if (fields[i].getType().isPrimitive()) {
                    if (this.hasDefaultValue(fields[i], binded)) { // default primitive value case
                        System.out.println("DefaultValueCase : we are on the field " + fields[i].getName());
                        throw new Exception(" The attribute " + fields[i].getName() + " is required and has to be set");
                    }
                } else {
                    if (this.getFieldValue(fields[i], binded) == null) {
                        throw new Exception(" The attribute " + fields[i].getName() + " is required and has to be set");
                    }
                }
            }
        }
    }

}
