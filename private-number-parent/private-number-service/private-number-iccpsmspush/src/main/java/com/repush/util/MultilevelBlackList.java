package com.repush.util;

import cn.hutool.core.util.StrUtil;
import com.repush.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 白名单过滤器
 * 创建时间：2018年2月28日 下午3:01:12
 * <p>
 * 思路： 创建一个FilterSet，枚举了0~65535的所有char是否是某个敏感词开头的状态
 * <p>
 * 判断是否是 敏感词开头 | | 是 不是 获取头节点 OK--下一个字 然后逐级遍历，DFA算法
 *
 * @author youngder
 * @version 2.2
 */
@Component
@Slf4j
public class MultilevelBlackList {

    private static final char SIGN = '*'; // 敏感词过滤替换
    //多级黑名单
    private static List<String> multilevelBlackStr = new ArrayList<>();
    //第三步 建一个静态的本类
    private static MultilevelBlackList multilevelBlackList;
    @Resource
    private SmsService smsService;

    /**
     * 过滤判断 返回其中包含的敏感词
     *
     * @param src
     * @return
     */
    public static final String doFilter(final String src) {
        String[] s = {};
        for (String backList : multilevelBlackStr) {
            if (StrUtil.isEmpty(backList)) {
                continue;
            }
            s = backList.split(",");
            int i = 0;
            for (String backWord : s) {
                if (src.contains(backWord)) {
                    i++;
                    if (i == s.length) {
                        //System.out.println(backList+"匹配到黑名单");
                        return backList;
                    }

                } else {
                    //System.out.println("backWord="+backWord+"backlist="+backList+"未匹配到黑名单");
                    continue;
                }
            }
        }
        return "";
    }

    public static void initMultilevelBlackList() {
        // 获取敏感词
        try {
            //需要改成数据库查询
            multilevelBlackStr = multilevelBlackList.smsService.findAllMultipleBlackList();
            log.info("多级黑名单长度：" + multilevelBlackStr.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        multilevelBlackList = this;
    }

}