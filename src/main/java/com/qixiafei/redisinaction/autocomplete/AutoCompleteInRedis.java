package com.qixiafei.redisinaction.autocomplete;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

/**
 * <P>Description: 利用redis的有序集合，有序集合中所有元素分值为0，则有序集合会将字符串看做二进制串进行排序，
 * 找到搜索关键字边界，比如26个英文字母边界就是ASCii码比a小1和ASCii码比z大1的字符，在搜索的最后一个关键字后加入边界，
 * 根据两个增加边界的元素放入zset，用zrange查询处于两个边界元素中间的结果，实现自动补全功能，适用于分组并不太多，内存不敏感的场景，例如游戏公会. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/13 15:44</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class AutoCompleteInRedis {

    @Resource
    private RedisClient redisClient;

    /**
     * 更新组织成员.
     *
     * @param org    组织名
     * @param member 成员
     * @param add    true-新增，false-删除
     */
    public void updateOrgMembers(final String org, final String member, boolean add) {
        final String key = getOrgSetKey(org);
        if (add) redisClient.zadd(key, 0, member);
        else redisClient.zrem(key, member);
    }

    /**
     * 根据关键词返回自动完成列表.
     *
     * @param org         组织名
     * @param keyWord     关键词
     * @param expectCount 期待的返回结果数量
     * @return 自动完成列表
     * @throws UnsupportedEncodingException
     */
    public Set<String> autoComplete(final String org, final String keyWord, final int expectCount) throws UnsupportedEncodingException {

        final String key = getOrgSetKey(org);
        final String floor = "[" + keyWord;
        byte[] bytes = keyWord.getBytes(StandardCharsets.UTF_8);
        // 如果关键字最后一个字节已经是最大字节，在最后追加10个最大字节
        if (bytes[bytes.length - 1] == Byte.MAX_VALUE) {
            byte[] newBytes = new byte[bytes.length + 10];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            Arrays.fill(newBytes, bytes.length, newBytes.length, Byte.MAX_VALUE);
            bytes = newBytes;
        } else {
            bytes[bytes.length - 1] = (byte) (bytes[bytes.length - 1] + 1);
        }
        final String ceiling = "(" + (new String(bytes, StandardCharsets.UTF_8));
        return redisClient.zrangeByLex(key, floor, ceiling, 0, expectCount);
    }

    private String getOrgSetKey(final String org) {
        return String.format("%s%s", RedisKeyConstants.ORG_MEMBER_PREFIX, org);
    }

}
