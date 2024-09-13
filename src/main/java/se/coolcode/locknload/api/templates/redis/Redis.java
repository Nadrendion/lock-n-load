package se.coolcode.locknload.api.templates.redis;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import se.coolcode.locknload.api.Lock;
import se.coolcode.locknload.api.templates.Template;

public class Redis implements Template {

    private final RedissonClient redissonClient;
    private final CacheConfig cacheConfig;
    private final LockConfig lockConfig;

    public Redis(RedissonClient redissonClient, CacheConfig cacheConfig, LockConfig lockConfig) {
        this.redissonClient = redissonClient;
        this.cacheConfig = cacheConfig;
        this.lockConfig = lockConfig;
    }

    @Override
    public Object get(String user, String resource) {
        String cacheKey = getCacheKey(user, resource);
        return redissonClient.getMapCache(cacheKey).get(resource);
    }

    @Override
    public void put(String user, String resource, Object data) {
        String cacheKey = getCacheKey(user, resource);
        redissonClient.getMapCache(cacheKey).put(resource, data, cacheConfig.leaseTime(), cacheConfig.timeUnit());
    }

    @Override
    public Lock getLock(String user, String resource) {
        String lockKey = getLockKey(user, resource);
        RLock lock = redissonClient.getLock(lockKey);
        return new RedisLock(lockKey, lock, lockConfig);
    }

    private String getCacheKey(String user, String resource) {
        return user != null ? user : "shared-resources";
    }

    private String getLockKey(String user, String resource) {
        return user != null ? user + "-" + resource : resource;
    }

}
