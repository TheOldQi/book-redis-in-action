package com.qixiafei.redisinaction;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 20:25</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
public class Demo {


    public static void main(String[] args) {
        int i = 5;
        int j = 6;
        System.out.println(j = i);
        System.out.println(j);
        Jedis jedis = new Jedis();
        final Pipeline pipelined = jedis.pipelined();
        pipelined.exec();
    }


}
