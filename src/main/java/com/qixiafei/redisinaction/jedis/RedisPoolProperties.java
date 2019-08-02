package com.qixiafei.redisinaction.jedis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <P>Description: jedis连接池配置. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/4/25 17:36</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Data
@ConfigurationProperties(prefix = "redis")
public class RedisPoolProperties {

    /**
     * 连接超时.
     */
    private int connectionTimeOut = 0;

    /**
     * 通讯超时.
     */
    private int soTimeOut = 0;

    /**
     * 密码.
     */
    private String password;

    /**
     * 最大连接数.
     */
    private int maxTotal = 10;

    /**
     * 最大空闲连接数.
     */
    private int maxIdle = 10;

    /**
     * 最小空闲连接数.
     */
    private int minIdle = 3;

    /**
     * 是否在空闲时测试连接可用状态.
     */
    private boolean testWhileIdle = false;

    /**
     * 单机redis配置.
     */
    private StandaloneConfig standalone;

    /**
     * 哨兵配置.
     */
    private SentinelConfig sentinel;

    @Data
    public static class StandaloneConfig {
        /**
         * 服务端访问ip.
         */
        private String host;

        /**
         * 服务端端口号.
         */
        private int port;
    }

    @Data
    public static class SentinelConfig {

        /**
         * 哨兵集群名称.
         */
        private String masterName;

        /**
         * 哨兵地址,用英文逗号分隔，例：127.0.0.1:7501,127.0.0.1:7502.
         */
        private String nodes;
    }


}
