package com.qixiafei.redisinaction;

import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@SpringBootApplication
public class RedisInActionApplication {

    private volatile boolean stop = false;

    public static void main(String[] args) {
        SpringApplication.run(RedisInActionApplication.class, args);
    }

    @Resource
    private RedisClient redisClient;

    @RequestMapping("redis/get/{key}")
    public String getKey(final @PathVariable String key) {
        return redisClient.get(key);
    }

    @RequestMapping("test/donothing")
    public String doNothing() {
        return "test";
    }

    @RequestMapping("test/withlog")
    public String withLog() {
        log.info("testtest{}", "test");
        return "test";
    }

    @RequestMapping("redis/set/{key}/{value}")
    public String redisSet(final @PathVariable String key, final @PathVariable String value) {
        return redisClient.set(key, value) + "";
    }

    @RequestMapping("redis/incr/{key}")
    public String incr(final @PathVariable String key) {
        return redisClient.incr(key) + "";
    }

    @RequestMapping("redis/pressure/start")
    public void startPressure() {
        stop = false;
        final String key = UUID.randomUUID().toString();
        log.info("pressure start,key is {}", key);
        Long num = 0L;
        long startMillis = System.currentTimeMillis();
        while (!stop) {
            num = redisClient.incr(key);
        }
        long elapase = (System.currentTimeMillis() - startMillis) / 1000;
        log.info("stop by user ,now is {},average per second is {} ", num, num / elapase);
    }


    @RequestMapping("redis/pressure/stop")
    public void stopPressure() {
        this.stop = true;
        log.info("call stop");
    }


    @RequestMapping("redis/blpop")
    public RedisClient.PopResult blpop() {
        return redisClient.blpop(2, "testList1", "testList2");
    }

    @RequestMapping("redis/spop/{key}")
    public String spop(@PathVariable final String key) {
        return redisClient.spop(key);
    }

    @RequestMapping("redis/spop/{key}/{count}")
    public Set<String> spopMulti(@PathVariable final String key, @PathVariable final int count) {
        return redisClient.spop(key, count);
    }

    @RequestMapping("redis/info")
    public String info() {
        final String info = redisClient.info();
        log.info("redis info={}", info);
        return info;
    }


    @RequestMapping("redis/watch")
    public List<Object> watch() throws InterruptedException {
        try (Jedis instance = redisClient.getInstance()) {
            final Pipeline pipelined = instance.pipelined();
            pipelined.watch("test");
            pipelined.sync();
            Thread.sleep(10000);
            pipelined.multi();
            pipelined.set("test", "newVal");
            final Response<List<Object>> result = pipelined.exec();
            // sync后response才能get
            pipelined.sync();
            // 如果watch的key被修改，exec返回null
            log.info("watch result = {}", result.get());
            return result.get();
        }

    }


}
