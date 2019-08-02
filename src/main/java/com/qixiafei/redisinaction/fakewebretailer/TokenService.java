package com.qixiafei.redisinaction.fakewebretailer;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <P>Description: 令牌操作相关类. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/4/26 15:09</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class TokenService {

    @Resource
    private RedisClient redisClient;

    public String checkToken(final String token) {
        return redisClient.hget(RedisKeyConstants.LOGIN_HASH_KEY, token);
    }

    public void updateToken(final String token, final String userId, final String item) {
        final long timestamp = System.currentTimeMillis();
        redisClient.hset(RedisKeyConstants.LOGIN_HASH_KEY, token, userId);
        redisClient.zadd(RedisKeyConstants.RECENT_ZSET_KEY, timestamp, token);
        if (StringUtils.isNotBlank(item)) {
            final String viewKey = RedisKeyConstants.VIEW_ZSET_KEY_PREFIX + token;
            redisClient.zadd(viewKey, timestamp, item);
            // 保留最近25条记录
            redisClient.zremRangeByRank(viewKey, 0, -26);
        }
    }
}
