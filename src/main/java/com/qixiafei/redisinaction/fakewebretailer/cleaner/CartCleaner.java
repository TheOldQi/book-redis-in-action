package com.qixiafei.redisinaction.fakewebretailer.cleaner;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <P>Description: 清除用户购物车信息. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 19:34</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class CartCleaner implements CustomCleaner {

    @Resource
    private RedisClient redisClient;

    @Override
    public void clean(String[] tokenArr) {
        final int size = tokenArr.length;
        final String[] cartKeyArr = new String[size];
        for (int i = 0; i < size; i++) {
            cartKeyArr[i] = RedisKeyConstants.CART_HASH_KEY_PREFIX + tokenArr[i];
        }
        redisClient.del(cartKeyArr);
    }

    @Override
    public void regLog() {
        log.info("CartCleaner reg success！");
    }
}
