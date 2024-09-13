package se.coolcode.locknload.api.templates.redis;

import org.redisson.api.RLock;
import se.coolcode.locknload.api.Lock;
import se.coolcode.locknload.api.exceptions.LockException;

public class RedisLock implements Lock {

    private final String lockKey;
    private final RLock lock;
    private final LockConfig lockConfig;

    public RedisLock(String lockKey, RLock lock, LockConfig lockConfig) {
        this.lockKey = lockKey;
        this.lock = lock;
        this.lockConfig = lockConfig;
    }

    @Override
    public boolean lock() {
        try {
            return lock.tryLock(lockConfig.waitTime(), lockConfig.leaseTime(), lockConfig.timeUnit());
        } catch (InterruptedException e) {
            throw new LockException("Failed to lock " + lockKey, e);
        }
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
