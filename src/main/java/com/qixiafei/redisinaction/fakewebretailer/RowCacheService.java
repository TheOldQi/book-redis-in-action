package com.qixiafei.redisinaction.fakewebretailer;

import com.alibaba.fastjson.JSON;
import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Tuple;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <P>Description: 缓存行的实现. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 21:27</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class RowCacheService {

    @Resource
    private RedisClient redisClient;

    public void defineCache(final String rowId, final long delay) {
        redisClient.zadd(RedisKeyConstants.DELAY_ZSET_KEY, delay, rowId);
        redisClient.zadd(RedisKeyConstants.SCHEDULE_ZSET_KEY, System.currentTimeMillis(), rowId);
    }

    @PostConstruct
    public void onInit() {
        Thread t = new Thread(new RowCacheDaemon(redisClient));
        t.setDaemon(true);
        t.start();
    }

    static class RowCacheDaemon implements Runnable {

        private RedisClient redisClient;

        RowCacheDaemon(RedisClient redisClient) {
            this.redisClient = redisClient;
        }

        @Override
        public void run() {

            while (true) {

                final Set<Tuple> tuples = redisClient.zrangeWithScores(RedisKeyConstants.SCHEDULE_ZSET_KEY, 0, 0);
                if (!tuples.isEmpty()) {
                    final Tuple next = tuples.iterator().next();
                    long nowStamp = System.currentTimeMillis();
                    if (next != null && next.getScore() <= nowStamp) {

                        final String rowId = next.getElement();
                        final long delay = (long) redisClient.zscore(RedisKeyConstants.DELAY_ZSET_KEY, rowId).doubleValue();
                        if (delay <= 0) {
                            redisClient.zrem(RedisKeyConstants.DELAY_ZSET_KEY, rowId);
                            redisClient.zrem(RedisKeyConstants.SCHEDULE_ZSET_KEY, rowId);
                            redisClient.del(RedisKeyConstants.INV_KEY_PREFIX + rowId);
                            continue;
                        }

                        final RowData rowData = new RowData();
                        redisClient.zadd(RedisKeyConstants.SCHEDULE_ZSET_KEY, nowStamp + delay, rowId);
                        redisClient.set(RedisKeyConstants.INV_KEY_PREFIX + rowId, JSON.toJSONString(rowData));

                    }

                }


                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Data
    public static class RowData {
        private String id;
        private String name;

        private String sex;
    }
}
