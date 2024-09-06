package se.coolcode.locknload.annotations;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    String resource();

    Strategy cache();

    Strategy lock();

    enum Strategy {
        BY_USER, BY_RESOURCE, DISABLE
    }
}
