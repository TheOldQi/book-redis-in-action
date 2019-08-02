package com.qixiafei.redisinaction;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/8 21:04</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
public class MySubcriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {

        log.info("onMessage ==> channel={},message={}", channel, message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        log.info("onPMessage ==> pattern={},channel={},message={}", pattern, channel, message);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        log.info("onSubscribe ==> channel={},subscribedChannels={}", channel, subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        log.info("onUnsubscribe ==> channel={},subscribedChannels={}", channel, subscribedChannels);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        log.info("onPUnsubscribe ==> pattern={},subscribedChannels={}", pattern, subscribedChannels);

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        log.info("onPSubscribe ==> pattern={},subscribedChannels={}", pattern, subscribedChannels);
    }

    @Override
    public void onPong(String pattern) {
        log.info("onPong ==> pattern={}", pattern);
    }

}
