package com.qixiafei.redisinaction.autocomplete;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <P>Description: 利用redis维护最常联系人列表，java整理自动补全结果，适用于集合较多，内存敏感的场景. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/13 15:16</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class AutoCompleteInJava {

    @Resource
    private RedisClient redisClient;

    private static final String IDEMPOTENT_PATTERN = "autocompolete:contract:%s:%s:";

    public void updateContract(final String user, final String contract) {
        final String key = String.format("%s%s", RedisKeyConstants.CONTRACT_LIST_PREFIX, user);
        final boolean nx = redisClient.setNotExist(String.format(IDEMPOTENT_PATTERN, user, contract), "", 1);
        if (nx) {
            redisClient.execPipeLine((pipelined) -> {
                pipelined.lrem(key, 1, contract);
                pipelined.lpush(key, contract);
                pipelined.ltrim(key, 0, 99);
                pipelined.sync();
            });

        }

    }

    public List<String> autoComplete(final String user, final String keyWord) {
        final String key = String.format("%s%s", RedisKeyConstants.CONTRACT_LIST_PREFIX, user);
        final List<String> contractList = redisClient.lrange(key, 0, -1);
        final String keyWordLowercase = keyWord.toLowerCase();
        final List<String> result = new ArrayList<>();
        for (int i = 0, len = contractList.size(); i < len; i++) {
            final String contract = contractList.get(i);
            final String contractLowercase = contract.toLowerCase();
            if (contractLowercase.startsWith(keyWordLowercase)) result.add(contract);
        }

        return result;
    }

}
