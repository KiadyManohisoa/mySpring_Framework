package annotation.permission;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Logged {

    Class<?>[] value() default {};

}
