package se.coolcode.locknload.util;

import se.coolcode.locknload.annotations.Cache;
import se.coolcode.locknload.annotations.Resource;
import se.coolcode.locknload.annotations.UserId;
import se.coolcode.locknload.api.Lock;
import se.coolcode.locknload.api.templates.Template;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CacheUtil {

    private CacheUtil() {}

    public static Lock getLock(Cache cacheAnnotation, String user, String resource, Template template) {
        return Cache.Strategy.BY_USER.equals(cacheAnnotation.lock()) ? template.getLock(user, resource) : template.getLock(null, resource);
    }

    public static String getUser(Method method, Object[] args) {
        return getAnnotatedParameterValues(UserId.class, method, args).getFirst();
    }

    public static String getResource(Cache cacheAnnotation, Method method, Object[] args) {
        List<String> resources = getAnnotatedParameterValues(Resource.class, method, args);
        resources.addFirst(cacheAnnotation.resource());
        return String.join("-", resources);
    }

    private static List<String> getAnnotatedParameterValues(Class<?> annotation, Method method, Object[] args) {
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
}
