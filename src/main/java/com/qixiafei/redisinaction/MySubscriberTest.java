package com.qixiafei.redisinaction;

import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/8 21:09</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class MySubscriberTest implements ApplicationContextAware {

    @Resource
    private RedisClient redisClient;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        final MySubcriber mySubcriber = new MySubcriber();
//
//        Thread t = new Thread(() -> {
//            log.info("开始测试subscribe");
//            redisClient.subscribe(mySubcriber, "channelA");
//            log.info("结束测试subscribe");
//
//        });
//        t.start();
//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        mySubcriber.unsubscribe("channelA");
    }
}
