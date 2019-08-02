package com.qixiafei.redisinaction.fakewebretailer;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <P>Description: 缓存请求. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 19:57</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class CacheService {

    private static final String INIT_INFO = "hello world";
    private static final int FIVE_MINUTES = 60 * 5;


    @Resource
    private RedisClient redisClient;

    public String getContent(final String key) {
        final String redisKey = RedisKeyConstants.CACHE_KEY_PREFIX + key;
        String content = redisClient.get(redisKey);
        if (StringUtils.isBlank(content)) {
            content = INIT_INFO;
            redisClient.setExpire(redisKey, content, FIVE_MINUTES);
        }
        return content;
    }

}
