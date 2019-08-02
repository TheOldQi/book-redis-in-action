package com.qixiafei.redisinaction.jedis;

import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.Pool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <P>Description: 封装redis方法. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/4/25 15:43</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
public class RedisClient {

    private Pool<Jedis> pool;

    private static final String SUCCESS_STR = "OK";

    private static final int DEFAULT_TIMEOUT = 2000;

    private static final String ADDRS_SPLITOR = ",";

    /**
     * 构建单机redis连接池.
     *
     * @param host              服务器ip
     * @param port              端口号
     * @param connectionTimeOut 连接超时，0代表不限制
     * @param soTimeOut         数据传输超时，0代表不限制
     * @param password          redis服务密码，可以为null
     * @param maxTotal          最大可用连接数
     * @param maxIdle           最大空闲连接数
     * @param minIdle           最小空闲连接数
     * @param testWhileIdle     是否在空闲时检测连接可用
     * @return redis操作客户端
     */
    public static RedisClient standAlonePool(final String host, final int port, final int connectionTimeOut, final int soTimeOut,
                                             final String password, final int maxTotal, final int maxIdle, final int minIdle,
                                             final boolean testWhileIdle) {


        final GenericObjectPoolConfig config = buildConfig(maxTotal, maxIdle, minIdle, testWhileIdle);
        return new RedisClient(new JedisPool(config, host, port, connectionTimeOut, soTimeOut, password, 0,
                "", false, null, null, null));
    }

    /**
     * 构建哨兵redis连接池.
     *
     * @param masterName        哨兵集群名称
     * @param nodes             哨兵地址，多个地址之间用英文逗号分隔
     * @param connectionTimeOut 连接超时，0代表不限制
     * @param soTimeOut         数据传输超时，0代表不限制
     * @param password          redis服务密码，可以为null
     * @param maxTotal          最大可用连接数
     * @param maxIdle           最大空闲连接数
     * @param minIdle           最小空闲连接数
     * @param testWhileIdle     是否在空闲时检测连接可用
     * @return
     */
    public static RedisClient sentinelPool(final String masterName, final String nodes, final String password,
                                           final int connectionTimeOut, final int soTimeOut, final int maxTotal,
                                           final int maxIdle, final int minIdle, final boolean testWhileIdle) {
        final GenericObjectPoolConfig config = buildConfig(maxTotal, maxIdle, minIdle, testWhileIdle);
        return new RedisClient(new JedisSentinelPool(masterName,
                new HashSet<>(Arrays.asList(nodes.split(ADDRS_SPLITOR))), config,
                connectionTimeOut, soTimeOut, password, 0));


    }


    // ==== 通用操作 start ===========================

    /**
     * 对指定key list、set、zset进行排序，从小到大顺序，集合内部必须全是数字（不一定是整数）.
     *
     * @param key 可以是list、set、zset的key
     * @return 排序结果
     */
    public List<String> sort(final String key) {
        try (Jedis resource = pool.getResource()) {
            return resource.sort(key);
        }
    }

    /**
     * 对指定key list、set、zset进行排序，按照参数排序，若alpha=true，按照二进制字符串排序，否则，集合内部必须全是数字（不一定是整数）.
     *
     * @param key           可以是list、set、zset的key
     * @param sortingParams 排序的参数，例SortingParams params = new SortingParams();params.desc();
     * @return 影响的key数量
     */
    public List<String> sort(final String key, final SortingParams sortingParams) {
        try (Jedis resource = pool.getResource()) {
            return resource.sort(key, sortingParams);
        }
    }

    /**
     * 对指定key list、set、zset进行排序，允许配置排序参数，若alpha=true，按照二进制字符串排序，否则，集合内部必须全是数字（不一定是整数）.
     *
     * @param key           可以是list、set、zset的key
     * @param sortingParams 排序的参数，例SortingParams params = new SortingParams();params.desc();
     * @param destKey       排序结果存放key （list格式存在）
     * @return key总数
     */
    public Long sort(final String key, final SortingParams sortingParams, final String destKey) {
        try (Jedis resource = pool.getResource()) {
            return resource.sort(key, sortingParams, destKey);
        }
    }

    /**
     * 移除key的过期时间.
     *
     * @param key 任何类型的key
     * @return 影响的key个数，key不存在0，否则1
     */
    public Long persist(final String key) {
        try (Jedis resource = pool.getResource()) {
            return resource.persist(key);
        }
    }

    /**
     * 设置key的过期时间.
     *
     * @param key            任何类型的key
     * @param expiredSeconds 过期时间（秒）
     * @return 影响的key数量，key不存在0，否则1
     */
    public Long expire(final String key, final int expiredSeconds) {
        try (Jedis resource = pool.getResource()) {
            return resource.expire(key, expiredSeconds);
        }
    }

    /**
     * 重新设置key的过期时间.
     *
     * @param key      任何类型的key
     * @param unixTime 过期时刻（System.currentTimeMillis()/1000）
     * @return 影响的key数量，key不存在0，否则1
     */
    public Long expireAt(final String key, final long unixTime) {
        try (Jedis resource = pool.getResource()) {
            return resource.expireAt(key, unixTime);
        }
    }

    /**
     * 查询key距离过期还有多少秒.
     *
     * @param key 任何类型的key
     * @return 永久对象返回-1，不存在对象返回-2，否则返回距离过期秒数
     */
    public Long ttl(final String key) {
        try (Jedis resource = pool.getResource()) {
            return resource.ttl(key);
        }
    }

    /**
     * 设置key的过期时间.
     *
     * @param key           任何类型的key
     * @param expiredMillis 过期时间（毫秒）
     * @return 影响的key数量，key不存在0，否则1
     */
    public Long pexpire(final String key, final int expiredMillis) {
        try (Jedis resource = pool.getResource()) {
            return resource.pexpire(key, expiredMillis);
        }
    }

    /**
     * 重新设置key的过期时间.
     *
     * @param key      任何类型的key
     * @param unixTime 过期时刻System.currentTimeMillis()
     * @return 影响的key数量，key不存在0，否则1
     */
    public Long pexpireAt(final String key, final long unixTime) {
        try (Jedis resource = pool.getResource()) {
            return resource.pexpireAt(key, unixTime);
        }
    }

    /**
     * 查询key距离过期还有多少豪秒.
     *
     * @param key 任何类型的key
     * @return 永久对象返回-1，不存在对象返回-2，否则返回距离过期秒数
     */
    public Long pttl(final String key) {
        try (Jedis resource = pool.getResource()) {
            return resource.pttl(key);
        }
    }

    /**
     * 删除不定项个key.
     *
     * @param keys 任何类型的不定项个key
     * @return 删除的key数量
     */
    public Long del(String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.del(keys);
        }
    }

    /**
     * 查看key的类型.
     *
     * @param key
     * @return
     */
    public KeyType type(final String key) {
        try (final Jedis resource = pool.getResource()) {
            final String type = resource.type(key);
            return KeyType.instance(type);
        }
    }

    /**
     * 重命名key.
     *
     * @param oldKey 原名
     * @param newKey 新名
     * @return 修改成功
     */
    public boolean rename(final String oldKey, final String newKey) {
        try (final Jedis resource = pool.getResource()) {
            return SUCCESS_STR.equals(resource.rename(oldKey, newKey));
        }
    }

    /**
     * 查询redis运行情况.
     *
     * @return 运行情况
     */
    public String info() {
        try (final Jedis resource = pool.getResource()) {
            return resource.info();
        }
    }

    /**
     * 查询redis运行情况.
     *
     * @param section 过滤条件
     * @return 运行情况
     */
    public String info(final String section) {
        try (final Jedis resource = pool.getResource()) {
            return resource.info(section);
        }
    }

    /**
     * 获取jedis实例，使用完务必调用close归还资源.
     */
    public Jedis getInstance() {
        return pool.getResource();
    }

    /**
     * 以管道形式执行若干操作.
     * 划重点：若使用了事务，watch的key在multi和exec 之间变化了，exec结果会是null
     */
    public void execPipeLine(PipelineTask task) {
        try (final Jedis resource = pool.getResource()) {
            task.exec(resource.pipelined());
        }
    }

    public interface PipelineTask {
        void exec(Pipeline pipeline);
    }

    // ==== 通用操作 end ===========================

    // ==== string 操作 start ===========================

    /**
     * 获取字符串的值.
     *
     * @param key key
     * @return 字符串的值，不存在返回null
     */
    public String get(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.get(key);
        }
    }

    /**
     * 设置字符串的值，没有过期时间.
     *
     * @param key   key
     * @param value value
     * @return 是否设置成功，只有在内存空间不足的时候才可能失败
     */
    public boolean set(final String key, final String value) {
        try (Jedis resource = pool.getResource()) {
            return SUCCESS_STR.equals(resource.set(key, value));
        }
    }

    /**
     * 设置字符串的值，同时设置过期时间.
     *
     * @param key   key
     * @param value value
     * @return 是否设置成功，只有在内存空间不足的时候才可能失败
     */
    public boolean setExpire(final String key, final String value, final int expiredSeconds) {
        try (Jedis resource = pool.getResource()) {
            return SUCCESS_STR.equals(resource.setex(key, expiredSeconds, value));
        }
    }

    /**
     * 设置字符串的值，不带过期时间.
     *
     * @param key   key
     * @param value value
     * @return 只有在不存在key的时候返回true
     */
    public boolean setNotExist(final String key, final String value) {
        try (Jedis resource = pool.getResource()) {
            final SetParams params = new SetParams();
            params.nx();
            return SUCCESS_STR.equals(resource.set(key, value, params));
        }
    }

    /**
     * 设置字符串的值，附带过期时间.
     *
     * @param key            key
     * @param value          value
     * @param expiredSeconds 超时时间（秒）
     * @return 只有在不存在key的时候返回true
     */
    public boolean setNotExist(final String key, final String value, final int expiredSeconds) {
        try (Jedis resource = pool.getResource()) {
            final SetParams params = new SetParams();
            params.ex(expiredSeconds);
            params.nx();
            return SUCCESS_STR.equals(resource.set(key, value, params));
        }
    }

    /**
     * 设置字符串的值，附带过期时间.
     *
     * @param key           key
     * @param value         value
     * @param expiredMillis 超时时间（豪秒）
     * @return 只有在不存在key的时候返回true
     */
    public boolean setNotExist(final String key, final String value, final long expiredMillis) {
        try (Jedis resource = pool.getResource()) {
            final SetParams params = new SetParams();
            params.px(expiredMillis);
            params.nx();
            return SUCCESS_STR.equals(resource.set(key, value, params));
        }
    }

    /**
     * 设置字符串的值，不带过期时间.
     *
     * @param key   key
     * @param value value
     * @return 只有在key已存在的时候返回true
     */
    public boolean setExist(final String key, final String value) {
        try (Jedis resource = pool.getResource()) {
            final SetParams params = new SetParams();
            params.xx();
            return SUCCESS_STR.equals(resource.set(key, value, params));
        }
    }


    /**
     * 设置字符串的值，附带过期时间.
     *
     * @param key            key
     * @param value          value
     * @param expiredSeconds 超时时间（秒）
     * @return 只有在key已存在的时候返回true
     */
    public boolean setExist(final String key, final String value, final int expiredSeconds) {
        try (Jedis resource = pool.getResource()) {
            final SetParams params = new SetParams();
            params.ex(expiredSeconds);
            params.xx();
            return SUCCESS_STR.equals(resource.set(key, value, params));
        }
    }

    /**
     * 让value自增1.
     *
     * @param key key
     * @return 若key不存在，返回1，若key存在返回key的value+1，若key不是合法整数 throws JedisDataException
     */
    public Long incr(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.incr(key);
        }
    }

    /**
     * 让value自减1.
     *
     * @param key key
     * @return 若key不存在，返回-1，若key存在返回key的value-1，若key不是合法整数 throws JedisDataException
     */
    public Long decr(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.decr(key);
        }
    }

    /**
     * 让value自增increment.
     *
     * @param key       key
     * @param increment 自增量
     * @return 自增后的value，若key不存在，返回increment相等数值，若key不是合法整数 throws JedisDataException
     */
    public Long incrBy(final String key, final long increment) {
        try (final Jedis resource = pool.getResource()) {
            return resource.incrBy(key, increment);
        }
    }

    /**
     * 让value自减decrement.
     *
     * @param key       key
     * @param decrement 自减量
     * @return 自减后的value，若key不存在，返回-decrement相等数值，若key不是合法整数 throws JedisDataException
     */
    public Long decrBy(final String key, final long decrement) {
        try (final Jedis resource = pool.getResource()) {
            return resource.decrBy(key, decrement);
        }
    }

    /**
     * 让value自增increment.
     *
     * @param key       key
     * @param increment 自增量
     * @return 自增后的value，若key不存在，返回increment相等数值
     */
    public Double incrByFloat(final String key, final double increment) {
        try (final Jedis resource = pool.getResource()) {
            return resource.incrByFloat(key, increment);
        }
    }

    /**
     * 在指定key后面追加value.
     *
     * @param key   key
     * @param value 追加内容
     * @return 追加后字符串长度
     */
    public Long append(final String key, final String value) {
        try (final Jedis resource = pool.getResource()) {
            return resource.append(key, value);
        }
    }

    /**
     * 获取字符串从start到end索引内的内容，包含start和end.
     * redis索引正数超标代表最后一个元素只有的元素，负数超标代表第一个元素
     *
     * @param key   key
     * @param start 开始索引，0是第一个，负数代表从末尾数，-1代表最后一个
     * @param end   结束索引，0是第一个，负数代表从末尾数，-1代表最后一个
     * @return 索引区间内的子串
     */
    public String getRange(final String key, final long start, final long end) {
        try (final Jedis resource = pool.getResource()) {
            return resource.getrange(key, start, end);
        }
    }

    /**
     * 将key的原值从offset位置开始替换成value串，超过原串末尾会增加长度.
     *
     * @param key    key
     * @param offset 偏移位置，从0开始，若超过原字符串长度，间隔补null字节00000000，应尽量避免出现这种情况
     * @param value  替换成的字符串
     * @return 设置之后字符串长度
     */
    public Long setRange(final String key, final long offset, final String value) {
        try (final Jedis resource = pool.getResource()) {
            return resource.setrange(key, offset, value);
        }
    }

    /**
     * 将字符串当成二进制查询指定bit位置是否为1.
     *
     * @param key      key
     * @param bitIndex bit位置
     * @return true-1，false-0，未初始化过的位置都会是0
     */
    public boolean getBit(final String key, final long bitIndex) {
        try (final Jedis resource = pool.getResource()) {
            return resource.getbit(key, bitIndex);
        }
    }

    /**
     * 将字符串当成二进制设置指定bit位置是否为1.
     *
     * @param key      key
     * @param bitIndex bit位置,未设置的间隔部分将补0
     * @param value    是否为1
     * @return 该bit位设置之前的值
     */
    public Boolean setBit(final String key, final long bitIndex, final boolean value) {
        try (final Jedis resource = pool.getResource()) {
            return resource.setbit(key, bitIndex, value);
        }
    }

    /**
     * 计算执行key的字符串二进制表示形式有多少个为1的bit.
     *
     * @param key key
     * @return bit位为1的数量
     */
    public long bitCount(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.bitcount(key);
        }
    }

    /**
     * 计算执行key的字符串start到end区间内二进制表示形式有多少个为1的bit.
     *
     * @param key   key
     * @param start 开始索引，0开始（包含）
     * @param end   结束索引，0开始（包含)
     * @return bit位为1的数量
     */
    public long bitCount(final String key, final long start, final long end) {
        try (final Jedis resource = pool.getResource()) {
            return resource.bitcount(key, start, end);
        }
    }

    /**
     * 位操作.
     *
     * @param op      位操作定义对象
     * @param destKey 计算结果存储key
     * @param srcKeys 参与操作的不定项个key
     * @return 计算结果字符串字符数
     */
    public long bitOp(final BitOP op, final String destKey, final String... srcKeys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.bitop(op, destKey, srcKeys);
        }
    }
    // =============string 操作 end =====================


    // =============list 操作 start =====================

    /**
     * 将一个或多个值推入列表右端，若key不存在则创建.
     *
     * @param key    key
     * @param values 推入列表的不定项个值
     * @return 推入后列表长度
     */
    public Long rpush(final String key, final String... values) {
        try (final Jedis resource = pool.getResource()) {
            return resource.rpush(key, values);
        }
    }

    /**
     * 将一个或多个值推入列表右端，当且仅当key列表存在.
     *
     * @param key    key
     * @param values 推入列表的不定项个值
     * @return 推入后列表长度，若key不存在，返回0
     */
    public Long rpushx(final String key, final String... values) {
        try (final Jedis resource = pool.getResource()) {
            return resource.rpushx(key, values);
        }
    }

    /**
     * 将一个或多个值推入列表左端，若key不存在则创建.
     *
     * @param key    key
     * @param values 推入列表的不定项个值
     * @return 推入后列表长度
     */
    public Long lpush(final String key, final String... values) {
        try (final Jedis resource = pool.getResource()) {
            return resource.lpush(key, values);
        }
    }

    /**
     * 将一个或多个值推入列表左端，当且仅当key列表存在.
     *
     * @param key    key
     * @param values 推入列表的不定项个值
     * @return 推入后列表长度，若key不存在，返回0
     */
    public Long lpushx(final String key, final String... values) {
        try (final Jedis resource = pool.getResource()) {
            return resource.lpushx(key, values);
        }
    }

    /**
     * 从列表右侧弹出一个元素(列表中移除).
     *
     * @param key key
     * @return 若列表空了，返回null，否则返回列表最右侧弹出元素
     */
    public String rpop(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.rpop(key);
        }
    }

    /**
     * 从列表左侧弹出一个元素(列表中移除).
     *
     * @param key key
     * @return 若列表空了，返回null，否则返回列表最左侧弹出元素
     */
    public String lpop(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.lpop(key);
        }
    }

    /**
     * 返回列表中offset位置的元素.
     *
     * @param key    key
     * @param offset 位置
     * @return offset位置元素，不存在返回null
     */
    public String lindex(final String key, final long offset) {
        try (final Jedis resource = pool.getResource()) {
            return resource.lindex(key, offset);
        }
    }

    /**
     * 获取列表指定范围内的元素.
     *
     * @param key   key
     * @param start 包含
     * @param end   包含
     * @return 列表指定范围内的元素，包含start,end
     */
    public List<String> lrange(final String key, final long start, final long end) {
        try (final Jedis resource = pool.getResource()) {
            return resource.lrange(key, start, end);
        }
    }

    /**
     * 从列表中删除元素.
     *
     * @param key   列表key
     * @param count 删除最大个数
     * @param item  元素
     * @return 实际删除个数
     */
    public Long lrem(final String key, final long count, final String item) {
        try (final Jedis resource = pool.getResource()) {
            return resource.lrem(key, count, item);
        }
    }

    /**
     * 裁剪list，保留在start到end区间内元素.
     *
     * @param key   key
     * @param start 包含
     * @param end   包含
     * @return true执行成功
     */
    public boolean ltrim(final String key, final long start, final long end) {
        try (final Jedis resource = pool.getResource()) {
            return SUCCESS_STR.equals(resource.ltrim(key, start, end));
        }
    }

    /**
     * 左到右第一个非空list弹出第一个元素后返回key和value.
     *
     * @param timeout 超时时间（秒）
     * @param keys    若干个list
     * @return 若达到超时时间，返回null
     */
    public PopResult blpop(final int timeout, final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            final List<String> result = resource.blpop(timeout, keys);
            return getPopResult(result);
        }
    }

    /**
     * 左到右第一个非空list弹出最后一个元素后返回key和value.
     *
     * @param timeout 超时时间（秒）
     * @param keys    若干个list
     * @return 从左到由第一个非空list弹出第一个元素后返回，若达到超时时间，返回null
     */
    public PopResult brpop(final int timeout, final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            final List<String> result = resource.brpop(timeout, keys);
            return getPopResult(result);
        }
    }

    /**
     * 将pop结果list转成PopResult对象.
     *
     * @param result pop结果
     * @return 封装后的结果
     */
    private PopResult getPopResult(List<String> result) {
        if (result == null) {
            return null;
        }
        final PopResult result1 = new PopResult();
        result1.setKey(result.get(0));
        result1.setValue(result.get(1));
        return result1;
    }

    @Data
    public static class PopResult {
        /**
         * 返回元素的list key.
         */
        private String key;

        /**
         * 返回的元素值.
         */
        private String value;
    }

    /**
     * 从srcKey右端弹出一个元素推入destKey左端.
     *
     * @param srcKey  从此list右端弹出
     * @param destKey 弹出元素推入此list
     * @return 若srcKey不为空，返回移动的元素
     */
    public String rpopLpush(final String srcKey, final String destKey) {
        try (final Jedis resource = pool.getResource()) {
            return resource.rpoplpush(srcKey, destKey);
        }
    }

    /**
     * 从srcKey右端弹出一个元素推入destKey左端，若srcKey空，阻塞直到srcKey不为空或超时.
     *
     * @param srcKey  从此list右端弹出
     * @param destKey 弹出元素推入此list
     * @param timeout 阻塞最长时间（秒）
     * @return 返回移动的元素，若超时，返回null
     */
    public String brpopLpush(final String srcKey, final String destKey, final int timeout) {
        try (final Jedis resource = pool.getResource()) {
            return resource.brpoplpush(srcKey, destKey, timeout);
        }
    }

    // =============list 操作 end =====================
    // =============set 操作 start =====================

    /**
     * 向set里添加若干个member.
     *
     * @param key     key
     * @param members members
     * @return 新添加member数量
     */
    public Long sadd(final String key, final String... members) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sadd(key, members);
        }
    }

    /**
     * 判断member是否存在于set.
     *
     * @param key    key
     * @param member member
     * @return true-存在，false-不存在
     */
    public boolean sismember(final String key, final String member) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sismember(key, member);
        }
    }

    /**
     * 从set中移除给定members.
     *
     * @param key     key
     * @param members members
     * @return 删除的数量
     */
    public Long srem(final String key, final String... members) {
        try (final Jedis resource = pool.getResource()) {
            return resource.srem(key, members);
        }
    }

    /**
     * 返回set中元素个数.
     *
     * @param key key
     * @return 元素个数, 计算不存在也是返回0
     */
    public Long scard(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.scard(key);
        }
    }

    /**
     * 返回set中所有元素.
     *
     * @param key key
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.所有元素，不存在返回空set
     */
    public Set<String /* member */> smembers(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.smembers(key);
        }
    }

    /**
     * 随机返回count个元素.
     *
     * @param key    key
     * @param count  返回随机元素个数，可以超过set大小
     * @param unique 是否唯一
     * @return 若key不存在，返回空set,若unique==false，返回元素个数等于count，若unique==true，返回最多count个不重复的set元素
     */
    public List<String /* member */> srandMember(final String key, final int count, final boolean unique) {
        try (final Jedis resource = pool.getResource()) {
            return resource.srandmember(key, unique ? count : -count);
        }
    }

    /**
     * 随机从set中弹出1个元素.
     *
     * @param key key
     * @return 1个set中的元素，若果没有返回null
     */
    public String spop(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.spop(key);
        }
    }

    /**
     * 随机从set中弹出count个元素.
     *
     * @param key   key
     * @param count 弹出元素个数
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.不大于count个set中的元素，若果没有返回空set
     */
    public Set<String /* member */> spop(final String key, final long count) {
        try (final Jedis resource = pool.getResource()) {
            return resource.spop(key, count);
        }
    }

    /**
     * 若srcKey set中包含member，将member从srcKey set 中移除放入destKey.
     *
     * @param member  元素值
     * @param srcKey  待移出set
     * @param destKey 待移入set
     * @return 1-srcKey中有member，0-没有
     */
    public Long smove(final String member, final String srcKey, final String destKey) {
        try (final Jedis resource = pool.getResource()) {
            return resource.smove(member, srcKey, destKey);
        }
    }

    /**
     * 返回那些第一个key 中有，后面Keys中没有的元素.
     *
     * @param keys 第一个key必传
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.第一个key中有，后面Keys中没有的元素.
     */
    public Set<String> sdiff(final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sdiff(keys);
        }
    }

    /**
     * 将那些第一个key 中有，后面Keys中没有的元素存入destkey.
     *
     * @param destKey 计算结果保存key
     * @param keys    第一个key必传
     * @return 第一个key中有，后面Keys中没有的元素个数.
     */
    public Long sdiffStore(final String destKey, final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sdiffstore(destKey, keys);
        }
    }

    /**
     * 返回所有set的交集.
     *
     * @param keys 很多key
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.交集
     */
    public Set<String> sinter(final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sinter(keys);
        }
    }

    /**
     * 所有keys的交集放入destKey.
     *
     * @param destKey 保存结果的key
     * @param keys    很多key
     * @return 交集个数
     */
    public Long sinterStore(final String destKey, final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sinterstore(destKey, keys);
        }
    }

    /**
     * 返回所有set的并集.
     *
     * @param keys 很多key
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.并集
     */
    public Set<String> sunion(final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sunion(keys);
        }
    }

    /**
     * 所有keys的并集放入destKey.
     *
     * @param destKey 保存结果的key
     * @param keys    很多key
     * @return 并集个数
     */
    public Long sunionStore(final String destKey, final String... keys) {
        try (final Jedis resource = pool.getResource()) {
            return resource.sunionstore(destKey, keys);
        }
    }

    // =============set 操作 end =====================

    // =============hash 操作 start =====================

    /**
     * hash结构获取指定field的values.
     *
     * @param key   key
     * @param field field
     * @return value
     */
    public String hget(final String key, final String field) {
        try (Jedis resource = pool.getResource()) {
            return resource.hget(key, field);
        }
    }

    /**
     * hash结构获取指定n个field的values.
     *
     * @param key    key
     * @param fields fields
     * @return value列表
     */
    public List<String> hmget(final String key, final String... fields) {
        try (Jedis resource = pool.getResource()) {
            return resource.hmget(key, fields);
        }
    }

    /**
     * 判断hash中是否存在field.
     *
     * @param key   key
     * @param field field
     * @return true-存在，false-不存在
     */
    public boolean hexists(final String key, final String field) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hexists(key, field);
        }
    }

    /**
     * 返回hash中所有field集合.
     *
     * @param key key
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.field set
     */
    public Set<String> hkeys(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hkeys(key);
        }
    }

    /**
     * 返回hash中所有value集合.
     *
     * @param key key
     * @return value set
     */
    public List<String> hvals(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hvals(key);
        }
    }

    /**
     * 获取hash中所有信息.
     *
     * @param key key
     * @return hash中的所有键值对
     */
    public Map<String /* field */, String /* value */> hgetAll(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hgetAll(key);
        }
    }

    /**
     * 将hash中一个field自增increment.
     *
     * @param key       key
     * @param field     field
     * @param increment 自增量
     * @return 自增后的value
     */
    public Long hincrBy(final String key, final String field, final long increment) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hincrBy(key, field, increment);
        }
    }

    /**
     * 将hash中一个field自增increment.
     *
     * @param key       key
     * @param field     field
     * @param increment 自增量
     * @return 自增后的value
     */
    public Double hincrByFloat(final String key, final String field, final double increment) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hincrByFloat(key, field, increment);
        }
    }

    /**
     * 设置hash的field的value，不存在新建.
     *
     * @param key   key
     * @param field field
     * @param value value
     * @return 新增field返回1，否则0
     */
    public Long hset(final String key, final String field, final String value) {
        try (Jedis resource = pool.getResource()) {
            return resource.hset(key, field, value);
        }
    }

    /**
     * 设置hash的多个field-value对，不存在新建.
     *
     * @param key  key
     * @param hash field-value对
     * @return 新增field的数量
     */
    public Long hset(final String key, final Map<String/* field */, String/* name */> hash) {
        try (Jedis resource = pool.getResource()) {
            return resource.hset(key, hash);
        }
    }

    /**
     * 设置hash的多个field-value对，不存在新建.
     *
     * @param key  key
     * @param hash field-value对
     * @return 新增成功
     */
    public boolean hmset(final String key, final Map<String/* field */, String/* name */> hash) {
        try (Jedis resource = pool.getResource()) {
            return SUCCESS_STR.equals(resource.hmset(key, hash));
        }
    }

    /**
     * 删除hash中的一个或多个field.
     *
     * @param key    key
     * @param fields 待删除的field
     * @return 删除的field数量
     */
    public Long hdel(final String key, final String... fields) {
        try (Jedis resource = pool.getResource()) {
            return resource.hdel(key, fields);
        }
    }

    /**
     * 计算hash中元素个数.
     *
     * @param key key
     * @return 元素总个数，key不存在返回0
     */
    public Long hlen(final String key) {
        try (final Jedis resource = pool.getResource()) {
            return resource.hlen(key);
        }
    }


    // =============hash 操作 end =====================

    // =============zset 操作 start =====================

    /**
     * 返回zset中member个数.
     *
     * @param key key
     * @return member个数，不存在key返回0o
     */
    public Long zcard(final String key) {
        try (Jedis resource = pool.getResource()) {
            return resource.zcard(key);
        }
    }

    /**
     * 返回zset中指定member的分数.
     *
     * @param key    key
     * @param member member
     * @return 不存在返回null，其他情况返回member的score
     */
    public Double zscore(final String key, final String member) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zscore(key, member);
        }
    }

    /**
     * 返回zset中指定member的排名.
     *
     * @param key    key
     * @param member member
     * @return member排名，从0开始，分数相等则按照字典序，所以rank也不会相同
     */
    public Long zrank(final String key, final String member) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrank(key, member);
        }
    }

    /**
     * 返回zset中指定member的排名（逆序）.
     *
     * @param key    key
     * @param member member
     * @return member排名，从zcard - 1开始，分数相等则按照字典序，所以rank也不会相同
     */
    public Long zrevRank(final String key, final String member) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrevrank(key, member);
        }
    }

    /**
     * 获取指定位置区间的member的set.
     *
     * @param key   zset的key
     * @param start 开始返回的member 位置，从0开始
     * @param stop  返回的最后一个member 位置，从0开始
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.member set
     */
    public Set<String> zrange(final String key, final long start, final long stop) {
        try (Jedis resource = pool.getResource()) {
            return resource.zrange(key, start, stop);
        }
    }

    /**
     * 获取指定位置区间的member的set（逆序）.
     *
     * @param key   zset的key
     * @param start 开始返回的member 位置，从0开始
     * @param stop  返回的最后一个member 位置，从0开始
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.member set
     */
    public Set<String> zrevRange(final String key, final long start, final long stop) {
        try (Jedis resource = pool.getResource()) {
            return resource.zrevrange(key, start, stop);
        }
    }

    /**
     * 获取指定位置区间的member和score的set.
     *
     * @param key   zset的key
     * @param start 开始返回的member 位置，从0开始
     * @param stop  返回的最后一个member 位置，从0开始
     * @return member set
     */
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long stop) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrangeWithScores(key, start, stop);
        }
    }

    /**
     * 获取指定位置区间的member和score的set（倒序）.
     *
     * @param key   zset的key
     * @param start 开始返回的member 位置，从0开始
     * @param stop  返回的最后一个member 位置，从0开始
     * @return member set
     */
    public Set<Tuple> zrevRangeWithScores(final String key, final long start, final long stop) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrevrangeWithScores(key, start, stop);
        }
    }

    /**
     * 获取zset处于score分数到toScore分数之间的member set.
     *
     * @param key       key
     * @param fromScore 开始返回的member 分数
     * @param toScore   终止返回member 分数
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.member set.
     */
    public Set<String> zrangeByScore(final String key, final double fromScore, final double toScore) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrangeByScore(key, fromScore, toScore);
        }
    }

    /**
     * 获取zset处于score分数到toScore分数之间的member set（倒序）.
     *
     * @param key       key
     * @param fromScore 开始返回的member 分数
     * @param toScore   终止返回member 分数
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.member set.
     */
    public Set<String> zrevRangeByScore(final String key, final double fromScore, final double toScore) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrevrangeByScore(key, fromScore, toScore);
        }
    }

    /**
     * 获取zset处于score分数到toScore分数之间的元素 set.
     *
     * @param key       key
     * @param fromScore 开始返回的member 分数
     * @param toScore   终止返回member 分数
     * @return 元素 set.
     */
    public Set<Tuple> zrangeByScoreWithScore(final String key, final double fromScore, final double toScore) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrangeByScoreWithScores(key, fromScore, toScore);
        }
    }

    /**
     * 获取zset处于score分数到toScore分数之间的元素set（倒序）.
     *
     * @param key       key
     * @param fromScore 开始返回的member 分数
     * @param toScore   终止返回member 分数
     * @return 元素 set.
     */
    public Set<Tuple> zrevRangeByScoreWithScore(final String key, final double fromScore, final double toScore) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrevrangeByScoreWithScores(key, fromScore, toScore);
        }
    }

    /**
     * 只有对score完全一致的zset才能使用，获取范围内的member集合.
     *
     * @param key     zset key
     * @param floor   最小值，必须以"["（闭区间）或"("（开区间）开头，或直接传"-"代表从第一个member开始
     * @param ceiling 最大值，必须以"["（闭区间）或"("（开区间）开头，或直接传"+"代表到最后一个结束
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.区间内的member，正序.
     */
    public Set<String> zrangeByLex(final String key, final String floor, final String ceiling) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrangeByLex(key, floor, ceiling);
        }
    }


    /**
     * 只有对score完全一致的zset才能使用，获取范围内的member集合.
     *
     * @param key     zset key
     * @param ceiling 最大值，必须以"["（闭区间）或"("（开区间）开头，或直接传"+"代表到最后一个结束
     * @param floor   最小值，必须以"["（闭区间）或"("（开区间）开头，或直接传"-"代表从第一个member开始
     * @param offset  从哪个位置开始查找（包含）
     * @param count   结果集最多包含多少元素
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.区间内的member，正序.
     */
    public Set<String> zrangeByLex(final String key, final String ceiling, final String floor,
                                   final int offset, final int count) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrangeByLex(key, ceiling, floor, offset, count);
        }
    }

    /**
     * 只有对score完全一致的zset才能使用，获取范围内的member集合.
     *
     * @param key     zset key
     * @param ceiling 最大值，必须以"["（闭区间）或"("（开区间）开头，或直接传"+"代表到最后一个结束
     * @param floor   最小值，必须以"["（闭区间）或"("（开区间）开头，或直接传"-"代表从第一个member开始
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.区间内的member，逆序.
     */
    public Set<String> zrevRangeByLex(final String key, final String ceiling, final String floor) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrevrangeByLex(key, ceiling, floor);
        }
    }

    /**
     * 只有对score完全一致的zset才能使用，获取范围内的member集合.
     *
     * @param key     zset key
     * @param ceiling 最大值，必须以"["（闭区间）或"("（开区间）开头，或直接传"+"代表到最后一个结束
     * @param floor   最小值，必须以"["（闭区间）或"("（开区间）开头，或直接传"-"代表从第一个member开始
     * @param offset  从哪个位置开始查找（包含）
     * @param count   结果集最多包含多少元素
     * @return 有序set，redis在list外部封装了一层，可以认为无法随机访问的list.区间内的member，逆序.
     */
    public Set<String> zrevRangeByLex(final String key, final String ceiling, final String floor,
                                      final int offset, final int count) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zrevrangeByLex(key, ceiling, floor, offset, count);
        }
    }

    /**
     * 统计zset 从 fromScore到toScore分数之间的member数量.
     *
     * @param key       key
     * @param fromScore 从这个分数开始统计（包含）
     * @param toScore   到这个分数统计结束（包含）
     * @return 从 fromScore到toScore分数之间的member数量
     */
    public Long zcount(final String key, final double fromScore, final double toScore) {
        try (final Jedis resource = pool.getResource()) {
            return resource.zcount(key, fromScore, toScore);
        }
    }

    /**
     * 向有序列表中添加member或更新member分值.
     *
     * @param key    key
     * @param score  分值
     * @param member member
     * @return 若member是新增，返回1，若只是更新值，返回0
     */
    public Long zadd(final String key, final double score, final String member) {
        try (Jedis resource = pool.getResource()) {
            return resource.zadd(key, score, member);
        }
    }

    /**
     * 向有序列表中批量添加member或更新member分值.
     *
     * @param key  key
     * @param hash key是member，value是分值
     * @return 新增member数量
     */
    public Long zadd(final String key, final Map<String/* member  */, Double/* score  */> hash) {
        try (Jedis resource = pool.getResource()) {
            return resource.zadd(key, hash);
        }
    }

    /**
     * zset中一个member的score自增increment，存在则创建.
     *
     * @param key       key
     * @param member    member
     * @param increment 自增量
     * @return 自增后的分数
     */
    public Double zincrBy(final String key, final double increment, final String member) {
        try (Jedis resource = pool.getResource()) {
            return resource.zincrby(key, increment, member);
        }
    }

    /**
     * 从zset中删除一个或多个members.
     *
     * @param key     key
     * @param members members
     * @return 成功删除的个数
     */
    public Long zrem(final String key, final String... members) {
        try (Jedis resource = pool.getResource()) {
            return resource.zrem(key, members);
        }
    }

    /**
     * 按照member排名范围从zset中删除元素.
     *
     * @param key   key
     * @param start 开始删除位置（包含）
     * @param stop  删除终止位置（包含）
     * @return 删除个数
     */
    public Long zremRangeByRank(final String key, final long start, final long stop) {
        try (Jedis resource = pool.getResource()) {
            return resource.zremrangeByRank(key, start, stop);
        }
    }

    /**
     * 按照member分数范围从zset中删除元素.
     *
     * @param key       key
     * @param fromScore 开始删除分数（包含）
     * @param toScore   删除终止分数（包含）
     * @return 删除个数
     */
    public Long zremrangeByScore(final String key, final double fromScore, final double toScore) {
        try (Jedis resource = pool.getResource()) {
            return resource.zremrangeByScore(key, fromScore, toScore);
        }
    }

    /**
     * 对zset执行交集计算，将结果存入destKey中.
     * 对于确认是交集的member，默认采用sum的方式合并score.
     *
     * @param destKey 结果zset,若destKey已存在，覆盖成计算结果（哪怕格式不是zset)
     * @param keys    参与计算的zset或set们，set的member会被当做score为1
     * @return 交集个数
     */
    public Long zinterStore(final String destKey, final String... keys) {
        try (Jedis resource = pool.getResource()) {
            return resource.zinterstore(destKey, keys);
        }
    }

    /**
     * 对zset执行并集计算，将结果存入destKey中.
     * 对于相同的member，默认采用sum的方式合并score.
     *
     * @param destKey 结果zset,若destKey已存在，覆盖成计算结果（哪怕格式不是zset)
     * @param keys    参与计算的zset或set们，set的member会被当做score为1
     * @return 并集个数
     */
    public Long zunionStore(final String destKey, final String... keys) {
        try (Jedis resource = pool.getResource()) {
            return resource.zunionstore(destKey, keys);
        }
    }


    // =============zset 操作 end =====================
    // =============发布订阅 操作 end =====================

    /**
     * 订阅若干个channel，请注意，此方法将阻塞线程，使用时请使用守护线程订阅，保存pubSubImpl对象，进行停止订阅操作.
     *
     * @param pubSubImpl jedis发布订阅类的实现
     * @param channels   订阅的频道
     */
    public void subscribe(final JedisPubSub pubSubImpl, final String... channels) {
        try (final Jedis resource = pool.getResource()) {
            resource.subscribe(pubSubImpl, channels);
        }
    }

    /**
     * 指定若干个正则表达式，订阅所有匹配的channel，请注意，此方法将阻塞线程，使用时请使用守护线程订阅，保存pubSubImpl对象，进行停止订阅操作.
     *
     * @param pubSubImpl jedis发布订阅类的实现
     * @param patterns   正则表达式
     */
    public void psubscribe(final JedisPubSub pubSubImpl, final String... patterns) {
        try (final Jedis resource = pool.getResource()) {
            resource.psubscribe(pubSubImpl, patterns);
        }
    }

    /**
     * 向channel中发布消息.
     *
     * @param channel channel
     * @param message 消息
     */
    public void publish(final String channel, final String message) {
        try (final Jedis resource = pool.getResource()) {
            resource.publish(channel, message);
        }
    }

    // =============发布订阅 操作 end =====================

    public enum KeyType {
        STRING("string"),
        LIST("list"),
        SET("set"),
        Z_SET("zset"),
        HASH("hash"),
        NOT_EXIST("none");

        private final String code;

        KeyType(final String code) {
            this.code = code;
        }

        private static KeyType instance(final String code) {
            if (code == null) return null;
            for (KeyType e : values())
                if (e.code.equals(code)) return e;
            return null;
        }

        public String getCode() {
            return code;
        }
    }


    private static GenericObjectPoolConfig buildConfig(final int maxTotal, final int maxIdle, final int minIdle,
                                                       final boolean testWhileIdle) {
        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal); // 最大连接数
        config.setMaxIdle(maxIdle); // 最大空闲连接数
        config.setMinIdle(minIdle); // 最小空闲连接数
        config.setLifo(false); // 后进先出，即优先使用最热点的连接
        config.setFairness(false); // 是否公平竞争
        config.setMaxWaitMillis(-1L); // 从池中borrowResource最大等待时间:-1无限等待
        config.setMinEvictableIdleTimeMillis(1800000L); // 空闲等待这个时间后可从池中逐出连接（不考虑minIdle)
        // 连接空闲时间大于softMinEvictableIdleTimeMillis并且当前连接池的空闲连接数大于最小空闲连接数minIdle
        config.setSoftMinEvictableIdleTimeMillis(-1L);
        // 一次探测几个连接可逐出
        config.setNumTestsPerEvictionRun(3);
        // 10000L
        config.setEvictorShutdownTimeoutMillis(10000L);
        config.setTestOnCreate(false);
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);
        config.setTestWhileIdle(testWhileIdle);
        config.setTimeBetweenEvictionRunsMillis(-1L);
        config.setBlockWhenExhausted(true);
        config.setJmxEnabled(true);
        config.setJmxNameBase("");
        config.setJmxNamePrefix("pool");
        return config;
    }

    private RedisClient(final Pool<Jedis> pool) {
        this.pool = pool;
    }

}
