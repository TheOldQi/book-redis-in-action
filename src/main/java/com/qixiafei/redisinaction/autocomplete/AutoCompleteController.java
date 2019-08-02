package com.qixiafei.redisinaction.autocomplete;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * <P>Description: . </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/14 10:27</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@RestController
public class AutoCompleteController {

    @Resource
    private AutoCompleteInRedis autoCompleteInRedis;

    @RequestMapping("/autocomplete/update/{org}/{member}/{add}")
    public String update(@PathVariable final String org, @PathVariable final String member, @PathVariable boolean add) {
        autoCompleteInRedis.updateOrgMembers(org, member, add);
        return "OK";
    }

    @RequestMapping("/autocomplete/getList/{org}/{keyword}/{count}")
    public Set<String> getList(@PathVariable final String org, @PathVariable final String keyword, @PathVariable int count) throws UnsupportedEncodingException {
        return autoCompleteInRedis.autoComplete(org, keyword, count);
    }
}
