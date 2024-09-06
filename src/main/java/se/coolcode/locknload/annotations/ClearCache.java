package se.coolcode.locknload.annotations;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClearCache {

    boolean prefix() default false;

    String resource();

    Strategy clear();

    enum Strategy {
        BY_PREFIX, BY_RESOURCE
    }
}
