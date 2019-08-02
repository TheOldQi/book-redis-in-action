package com.qixiafei.redisinaction.fakewebretailer.cleaner;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <P>Description: 清除登录信息. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 18:40</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class LoginCleaner implements CustomCleaner {

    @Resource
    private RedisClient redisClient;

    @Override
    public void clean(String[] tokenArr) {
        redisClient.hdel(RedisKeyConstants.LOGIN_HASH_KEY, tokenArr);
        log.info("");
    }

    @Override
    public void regLog() {
        log.info("LoginCleaner reg success！");
    }
}
