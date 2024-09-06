package se.coolcode.locknload.api;

import java.lang.reflect.Method;

public interface Template {

    <T> T get(Method method, Object[] args);
    <T> void put(T data, Method method, Object[] args);
    Lock getLock(Method method, Object[] args);
}
