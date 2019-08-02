package com.qixiafei.redisinaction;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 17:26</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
public final class RedisKeyConstants {

    // 用户登录信息hash的key
    public static final String LOGIN_HASH_KEY = "login:";
    // token最近登录信息zset的key
    public static final String RECENT_ZSET_KEY = "recent:";
    // token最近浏览信息zset的key前缀
    public static final String VIEW_ZSET_KEY_PREFIX = "view:";
    // 用户购物车hash的key前缀
    public static final String CART_HASH_KEY_PREFIX = "cart:";
    // 缓存请求的key前缀
    public static final String CACHE_KEY_PREFIX = "cache:";
    // 数据行缓存延迟时间zset key
    public static final String DELAY_ZSET_KEY = "delay:";
    // 数据行缓存调度zset key
    public static final String SCHEDULE_ZSET_KEY = "schedule:";
    // 行数据缓存string key
    public static final String INV_KEY_PREFIX = "inv:";
    // 最近联系人列表前缀
    public static final String CONTRACT_LIST_PREFIX = "recent:contract:";
    // 组织成员zset前缀
    public static final String ORG_MEMBER_PREFIX = "org:member:";


    private RedisKeyConstants() {
    }
}
