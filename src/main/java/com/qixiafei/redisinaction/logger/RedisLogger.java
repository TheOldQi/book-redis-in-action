package com.qixiafei.redisinaction.logger;

import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.annotation.Resource;

/**
 * <P>Description: 5.1日志. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/13 9:52</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class RedisLogger {

    @Resource
    private RedisClient redisClient;

    /**
     * 记录日志.
     *
     * @param name    日志名称，不同名称日志相互隔离
     * @param level   日志级别
     * @param message 日志消息
     */
    public void logRecent(final String name, final Level level, final String message) {
        try (Jedis instance = redisClient.getInstance()) {
            final Pipeline pipelined = instance.pipelined();
            log0(name, level, message, pipelined);
        }

    }

    private void log0(final String name, final Level level, final String message, final Pipeline pipeline) {
        final String key = String.format("recent:%s:" + name, level.key);
        pipeline.lpush(key, message);
        pipeline.ltrim(key, 0, 99);
        pipeline.sync();
    }


    public enum Level {

        DEBUG("logger:debug:"),
        INFO("logger:info:"),
        WARNING("logger:warning:"),
        ERROR("logger:error:");

        private String key;

        Level(final String key) {
            this.key = key;
        }
    }
}
