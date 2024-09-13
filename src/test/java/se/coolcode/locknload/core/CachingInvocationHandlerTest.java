package se.coolcode.locknload.core;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import se.coolcode.locknload.annotations.Cache;
import se.coolcode.locknload.annotations.UserId;
import se.coolcode.locknload.api.CachingInvocationHandler;
import se.coolcode.locknload.api.templates.Template;

import static se.coolcode.locknload.annotations.Cache.Strategy;

import java.lang.reflect.Proxy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CachingInvocationHandlerTest {

    public static final String DATA = "data";

    @Nested
    class Builder {

        @Test
        void creates_a_proxy() {
            Wrapped proxy = CachingInvocationHandler.builder(Wrapped.class, Mockito.mock(Wrapped.class)).build();

            Assertions.assertNotNull(proxy);
            Assertions.assertTrue(Proxy.isProxyClass(proxy.getClass()));
        }
    }

    @Nested
    class Invoke {

        @Test
        void calls_a_non_cache_annotated_method_without_side_effects() {
            Wrapped target = Mockito.mock(Wrapped.class);
            Template template = Mockito.mock(Template.class);
            Wrapped proxy = CachingInvocationHandler.builder(Wrapped.class, target).withTemplate(template).build();

            proxy.nonAnnotatedMethod();

            Mockito.verify(target, Mockito.times(1)).nonAnnotatedMethod();
            Mockito.verifyNoInteractions(template);
        }

        @Test
        void returns_value_from_a_called_non_cache_annotated_method() {
            Wrapped target = Mockito.mock(Wrapped.class);
            Mockito.when(target.nonAnnotatedMethodWithReturn()).thenReturn(DATA);
            Template template = Mockito.mock(Template.class);
            Wrapped proxy = CachingInvocationHandler.builder(Wrapped.class, target).withTemplate(template).build();

            String result = proxy.nonAnnotatedMethodWithReturn();

            Assertions.assertEquals(DATA, result);
            Mockito.verifyNoInteractions(template);
        }

        @Test
        void caches_returned_value_on_a_cache_by_user_annotated_method() {
            Wrapped target = Mockito.mock(Wrapped.class);
            Mockito.when(target.cacheByUser("ssn")).thenReturn(DATA);
            Template template = Mockito.mock(Template.class);
            Wrapped proxy = CachingInvocationHandler.builder(Wrapped.class, target).withTemplate(template).build();

            String result = proxy.cacheByUser("ssn");

            Assertions.assertEquals(DATA, result);
            Mockito.verify(template, Mockito.times(1)).get("ssn", "cacheByUser");
            Mockito.verify(template, Mockito.times(1)).put("ssn", "cacheByUser", DATA);
        }

        @Test
        void caches_returned_value_on_a_cache_by_resource_annotated_method() {
            Assertions.fail("Not implemented yet");
        }

        @Test
        void throws_exception_when_user_id_annotation_is_missing_on_a_cache_by_user_annotated_method() {
            Assertions.fail("Not implemented yet");
        }
    }

    public interface Wrapped {

        void nonAnnotatedMethod();

        String nonAnnotatedMethodWithReturn();

        @Cache(resource = "cacheByUser", cache = Strategy.BY_USER, lock = Strategy.DISABLE)
        String cacheByUser(@UserId String userid);
    }
}
