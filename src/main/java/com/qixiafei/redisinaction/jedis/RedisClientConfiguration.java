package com.qixiafei.redisinaction.jedis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/4/25 15:41</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@EnableConfigurationProperties(value = {RedisPoolProperties.class})
@Configuration
public class RedisClientConfiguration {

    @Resource
    private RedisPoolProperties prop;

    @Bean
    @ConditionalOnProperty(prefix = "redis.standalone", name = "host")
    @ConditionalOnMissingBean(value = RedisClient.class)
    public RedisClient standaloneClient() {
        return RedisClient.standAlonePool(prop.getStandalone().getHost(), prop.getStandalone().getPort(),
                prop.getConnectionTimeOut(), prop.getSoTimeOut(), prop.getPassword(), prop.getMaxTotal(),
                prop.getMaxIdle(), prop.getMinIdle(), prop.isTestWhileIdle());
    }

    @Bean
    @ConditionalOnProperty(prefix = "redis.sentinel", name = "nodes")
    @ConditionalOnMissingBean(value = RedisClient.class)
    public RedisClient sentinelClient() {
        return RedisClient.sentinelPool(prop.getSentinel().getMasterName(), prop.getSentinel().getNodes(),
                prop.getPassword(), prop.getConnectionTimeOut(), prop.getSoTimeOut(), prop.getMaxTotal(),
                prop.getMaxIdle(), prop.getMinIdle(), prop.isTestWhileIdle());
    }

}
