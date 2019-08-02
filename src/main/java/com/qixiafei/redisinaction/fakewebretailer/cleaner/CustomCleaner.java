package com.qixiafei.redisinaction.fakewebretailer.cleaner;

/**
 * <P>Description: 额外的清理器. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 18:29</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
public interface CustomCleaner {

    /**
     * 执行清理.
     *
     * @param tokenArr 要清理的token数组
     */
    void clean(final String[] tokenArr);

    /**
     * 打印注册成功日志.
     */
    void regLog();
}
