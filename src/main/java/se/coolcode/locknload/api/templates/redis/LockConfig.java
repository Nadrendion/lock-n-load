package se.coolcode.locknload.api.templates.redis;

import java.util.concurrent.TimeUnit;

public record LockConfig(long waitTime, long leaseTime, TimeUnit timeUnit) {
}
