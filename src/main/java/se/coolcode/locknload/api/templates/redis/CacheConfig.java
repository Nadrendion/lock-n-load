package se.coolcode.locknload.api.templates.redis;

import java.util.concurrent.TimeUnit;

public record CacheConfig(long leaseTime, TimeUnit timeUnit) {
}
