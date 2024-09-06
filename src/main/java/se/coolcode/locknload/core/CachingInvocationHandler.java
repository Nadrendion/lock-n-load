package se.coolcode.locknload.core;

import se.coolcode.locknload.annotations.Cache;
import se.coolcode.locknload.annotations.Resource;
import se.coolcode.locknload.api.Lock;
import se.coolcode.locknload.api.Template;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

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
                if (cacheAnnotation.lock() != Cache.Strategy.DISABLE) {
                    return executeLocked(cacheAnnotation, method, args);
                } else {
                    return executeCached(cacheAnnotation, method, args);
                }
            }
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object executeLocked(Cache cacheAnnotation, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Lock lock = null;
        try {
            Object data = template.get(method, args);
            if (data == null) {
                lock = template.getLock(method, args);
                if (lock.lock()) {
                    data = executeCached(cacheAnnotation, method, args);
                } else {
                    throw new LockException("Failed to lock resource: " + getResource(cacheAnnotation, method, args) + ".");
                }
            }
            return data;
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private Object executeCached(Cache cacheAnnotation, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Object data = template.get(method, args);
        if (data == null) {
            data = method.invoke(target, args);
            template.put(data, method, args);
        }
        return data;
    }

    private String getResource(Cache cacheAnnotation, Method method, Object[] args) {
        List<String> resources = getAnnotatedParameterValues(Resource.class, method, args);
        resources.addFirst(cacheAnnotation.resource());
        return String.join("-", resources);
    }

    private List<String> getAnnotatedParameterValues(Class<?> annotation, Method method, Object[] args) {
        int i = 0;
        List<String> values = new ArrayList<>();
        for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
            for (Annotation parameterAnnotation : parameterAnnotations) {
                if (parameterAnnotation.annotationType().equals(annotation)) {
                    values.add(args[i].toString());
                }
            }
            i++;
        }
        return values;
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
