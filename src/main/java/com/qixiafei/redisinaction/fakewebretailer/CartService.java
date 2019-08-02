package com.qixiafei.redisinaction.fakewebretailer;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <P>Description: 购物车服务. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 18:23</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class CartService {

    @Resource
    private RedisClient redisClient;

    /**
     * 更新购物车中商品状态.
     *
     * @param token   用户令牌
     * @param goodsId 商品id
     * @param num     商品数量
     */
    public void updateCart(final String token, final String goodsId, final int num) {
        if (num <= 0) {
            redisClient.hdel(RedisKeyConstants.CART_HASH_KEY_PREFIX + token, goodsId);
        } else {
            redisClient.hset(RedisKeyConstants.CART_HASH_KEY_PREFIX + token, goodsId, String.valueOf(num));
        }
    }

}
