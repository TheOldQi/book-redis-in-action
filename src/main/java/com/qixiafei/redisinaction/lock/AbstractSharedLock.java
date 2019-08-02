package com.qixiafei.redisinaction.lock;

import com.qixiafei.redisinaction.jedis.RedisClient;
import javax.annotation.Resource;
import java.util.UUID;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/14 17:54</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
public abstract class AbstractSharedLock implements ISharedLockSupport {

    /**
     * 尝试获取锁，失败了不重试，直接返回.
     *
     * @param lockName 锁名称
     * @param expired  锁过期时间（毫秒）
     * @return 若获取锁失败返回null，若获取成功返回锁id
     */
    public final String tryAcquire(final String lockName, final long expired) {
        final String id = UUID.randomUUID().toString();
        return lock0(lockName, expired, id);
    }


    /**
     * 尝试获取锁，失败重试，最多等待timeout毫秒.
     *
     * @param lockName 锁名称
     * @param expired  锁过期时间（毫秒）
     * @param timeout  等待获取锁超时（毫秒）
     * @return 若获取锁失败返回null，若获取成功返回锁id
     */
    public final String acquire(final String lockName, final long expired, final long timeout) {
        final String id = UUID.randomUUID().toString();
        final long timeoutStamp = System.currentTimeMillis() + timeout;
        do {
            final String lock = lock0(lockName, expired, id);
            if (lock != null) {
                return lock;
            }
        } while (System.currentTimeMillis() <= timeoutStamp);

        return null;
    }

    /**
     * 释放锁，利用watch机制确保删除的是id的锁，若watch失败了，无所谓，肯定是过期了被别人获取了，放弃就好.
     *
     * @param lockName 锁名称
     * @param id       锁uuid
     */
    public final void release(final String lockName, final String id) {
        release0(lockName,id);
    }

    protected abstract String getLockKey(final String lockName);

    protected abstract String lock0(final String lockName, final long expired, final String id);

    protected abstract void release0(final String lockName, final String id);

}
