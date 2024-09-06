package se.coolcode.locknload.core;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import se.coolcode.locknload.annotations.Cache;
import static se.coolcode.locknload.annotations.Cache.Strategy;

import java.lang.reflect.Proxy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CachingInvocationHandlerTest {

    public static final String DATA = "data";

    /**
     * Skicka in ett objekt som vet hur man cachar, rensar i redis (redisson-wrapper) till buildern. Template-pattern
     */

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
            Wrapped proxy = CachingInvocationHandler.builder(Wrapped.class, target).build();

            proxy.nonAnnotatedMethod();

            Mockito.verify(target, Mockito.times(1)).nonAnnotatedMethod();
        }

        @Test
        void returns_value_from_a_called_non_cache_annotated_method() {
            Wrapped target = Mockito.mock(Wrapped.class);
            Mockito.when(target.nonAnnotatedMethodWithReturn()).thenReturn(DATA);
            Wrapped proxy = CachingInvocationHandler.builder(Wrapped.class, target).build();

            String result = proxy.nonAnnotatedMethodWithReturn();

            Assertions.assertEquals(DATA, result);
        }

        @Test
        void caches_returned_value_on_a_cache_annotated_method_by_user() {
            Assertions.fail("Not implemented yet");
        }

        @Test
        void caches_returned_value_on_a_cache_annotated_method_by_resource() {
            Assertions.fail("Not implemented yet");
        }
    }

    interface Wrapped {

        void nonAnnotatedMethod();

        String nonAnnotatedMethodWithReturn();

        @Cache(resource = "cacheByUser", cache = Strategy.BY_USER, lock = Strategy.DISABLE)
        String cacheByUser(String userid);
    }
}
