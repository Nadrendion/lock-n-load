package se.coolcode.locknload.api;

import se.coolcode.locknload.annotations.Cache;
import se.coolcode.locknload.api.exceptions.InvocationException;
import se.coolcode.locknload.api.exceptions.LockException;
import se.coolcode.locknload.api.templates.Template;
import se.coolcode.locknload.util.CacheUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CachingInvocationHandler implements InvocationHandler {

    private final Object target;
    private final Template template;

    private CachingInvocationHandler(Object target, Template template) {
        this.target = target;
        this.template = template;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Cache cacheAnnotation = method.getAnnotation(Cache.class);
            if (cacheAnnotation != null) {
                String user = CacheUtil.getUser(method, args);
                String resource = CacheUtil.getResource(cacheAnnotation, method, args);
                if (cacheAnnotation.lock() != Cache.Strategy.DISABLE) {
                    return executeLocked(cacheAnnotation, method, args, user, resource);
                } else {
                    return executeCached(method, args, user, resource);
                }
            }
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (IllegalAccessException e) {
            throw new InvocationException("Failed to invoke " + method.getName(), e);
        }
    }

    private Object executeLocked(Cache cacheAnnotation, Method method, Object[] args, String user, String resource) throws InvocationTargetException, IllegalAccessException {
        Lock lock = null;
        try {
            Object data = template.get(user, resource);
            if (data == null) {
                lock = CacheUtil.getLock(cacheAnnotation, user, resource, template);
                if (lock.lock()) {
                    data = executeCached(method, args, user, resource);
                } else {
                    throw new LockException(String.format("Failed to lock resource: %s.", resource));
                }
            }
            return data;
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private Object executeCached(Method method, Object[] args, String user, String resource) throws InvocationTargetException, IllegalAccessException {
        Object data = template.get(user, resource);
        if (data == null) {
            data = method.invoke(target, args);
            template.put(user, resource, data);
        }
        return data;
    }

    public static <T, I extends T> Builder<T, I> builder(Class<T> type, I target) {
        return new Builder<>(type, target);
    }

    public static class Builder<T, I extends T> {

        private final Class<T> type;
        private final I target;
        private Template template;

        private Builder(Class<T> type, I target) {
            this.type = type;
            this.target = target;
        }

        public Builder<T, I> withTemplate(Template template) {
            this.template = template;
            return this;
        }

        @SuppressWarnings("unchecked")
        public T build() {
            return (T) Proxy.newProxyInstance(type.getClassLoader(),
                    new Class[]{type},
                    new CachingInvocationHandler(target, template));
        }
    }
}
