package com.qixiafei.redisinaction.lock;

import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.annotation.Resource;

/**
 * <P>Description: 分布式锁. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/14 16:01</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class DefaultLock extends AbstractSharedLock {

    private static final String LOCK_PATTERN = "s:default:lock:%s";

    @Resource
    private RedisClient redisClient;

    @Override
    protected String lock0(final String lockName, final long expired, final String id) {
        if (redisClient.setNotExist(getLockKey(lockName), id, expired)) {
            return id;
        }
        return null;
    }

    @Override
    protected void release0(final String lockName, final String id) {
        try (final Jedis instance = redisClient.getInstance()) {
            final Pipeline pipe = instance.pipelined();
            final String key = getLockKey(lockName);
            pipe.watch(key);
            final Response<String> lockId = pipe.get(key);
            pipe.sync();
            if (id.equals(lockId.get())) {
                pipe.multi();
                pipe.del(key);
                pipe.exec();
                pipe.sync();
            } else {
                instance.unwatch();
            }
        }
    }

    @Override
    protected String getLockKey(final String lockName) {
        return String.format(LOCK_PATTERN, lockName);
    }
}
