package com.cqt.monitor.web.callevent.xxjob;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.xxljob.config.XxlJobProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.Map;

/**
 * XXL-JOB 工具类
 *
 * @author scott
 * @date 2022年07月04日 17:58
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XxlJobUtil {
    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    private final XxlJobProperties xxlJobProperties;
    public static final String LOGIN_IDENTITY_KEY = "XXL_JOB_LOGIN_IDENTITY";

    public static final String JOB_INFO_ADD_URI = "jobinfo/add";
    public static final String JOB_INFO_UPDATE_URI = "jobinfo/update";
    public static final String JOB_INFO_START_URI = "jobinfo/start";
    public static final String JOB_INFO_STOP_URI = "jobinfo/stop";
    public static final String JOB_INFO_REMOVE_URI = "jobinfo/remove";


    /**
     * 登录XXL-JOB 获取cookie
     */
    public HttpCookie getCookie() {
        if (redissonUtil.isExistString(LOGIN_IDENTITY_KEY)) {
            return new HttpCookie(LOGIN_IDENTITY_KEY, redissonUtil.getString(LOGIN_IDENTITY_KEY));
        }

        long start = System.currentTimeMillis();

        // 设置登录参数
        LoginParam loginParam = LoginParam.builder()
                .userName(xxlJobProperties.getUserName())
                .password(xxlJobProperties.getPassword())
                .build();

        Map<String, Object> formMap = Convert.toMap(String.class, Object.class, loginParam);

        String reqUrl = getReqUrl(LoginParam.URI);
        log.info("[xxl-job] 请求xxl-job获取登录认证标识, 请求参数:{}, 请求地址:{}", formMap, reqUrl);
        HttpResponse response = HttpRequest.of(reqUrl)
                .method(LoginParam.METHOD)
                .form(formMap)
                .execute();
        log.info("[xxl-job] 请求xxl-job获取登录认证标识, 返回参数:{}, 耗时:{} ms", response, System.currentTimeMillis() - start);
        String cookieStr = response.getCookieValue(LOGIN_IDENTITY_KEY);

        log.info("[xxl-job] 请求xxl-job获取登录认证标识为: {}", cookieStr);
        redissonUtil.setString(LOGIN_IDENTITY_KEY, cookieStr);
        // 如果没有获取到cookie, 则说明登录失败了
        if (StrUtil.isBlank(cookieStr)) {
            ReturnT<?> returnT = JSON.parseObject(response.body(), ReturnT.class);
            throw new ParamCheckException("登录XXL-JOB失败, " + returnT.getMsg());
        }
        return new HttpCookie(LOGIN_IDENTITY_KEY, cookieStr);
    }

    /**
     * 通用请求XXL-JOB
     *
     * @param uri    接口URI
     * @param params 请求参数
     */
    private <T> ReturnT<T> commonCallXxlJob(String uri, Object params) {
        // 获取xxl-job的cookie
        HttpCookie cookie = getCookie();
        // 调用xxl-job接口
        HttpResponse response = commonCallXxlJob(uri, params, cookie);
        // 如果HTTP状态码为302, 则说明cookie过期或无效
        if (HttpStatus.HTTP_MOVED_TEMP == response.getStatus()) {
            // 先把redis中的cookie删除掉
            redissonUtil.delKey(LOGIN_IDENTITY_KEY);
            // 然后重新登录, 获取有效的cookie
            cookie = getCookie();
            response = commonCallXxlJob(uri, params, cookie);
        }
        // 如果HTTP状态码为2xx, 则返回对应报文
        if (response.isOk()) {
            try {
                return objectMapper.readValue(response.body(), new TypeReference<ReturnT<T>>() {
                });
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                throw new BusinessException("xxl-job 返回内容异常");
            }
        }
        throw new BusinessException("xxl-job 接口异常: " + response.body());
    }

    /**
     * 通用请求XXL-JOB
     *
     * @param uri    接口URI
     * @param params 请求参数
     */
    private HttpResponse commonCallXxlJob(String uri, Object params, HttpCookie cookie) {
        long startTime = System.currentTimeMillis();
        HttpResponse response = null;
        try {
            String reqUrl = getReqUrl(uri);
            Map<String, Object> formMap = Convert.toMap(String.class, Object.class, params);
            log.info("[xxl-job] 请求xxl-job的 {} 接口, 请求参数:{}, 请求地址:{}, cookie:{}", uri, params, reqUrl, cookie);
            response = HttpRequest.of(reqUrl)
                    .method(Method.POST)
                    .form(formMap)
                    .cookie(cookie)
                    .execute();
            return response;
        } finally {
            log.info("[xxl-job] 请求xxl-job获取登录认证标识, 返回参数:{}, 耗时:{} ms", response, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 拼接完整接口请求地址
     *
     * @param uri 接口URI
     * @return String 完整接口请求地址
     */
    private String getReqUrl(String uri) {
        return xxlJobProperties.getAdminAddresses() + uri;
    }


    /**
     * XXL-JOB 添加定时任务
     *
     * @param jobDesc         任务描述
     * @param groupId         对应任务所在的任务组ID
     * @param scheduleConf    CRON表达式
     * @param executorHandler JobHandler
     * @return jobId
     */
    public Integer addJob(String jobDesc, Integer groupId, String scheduleConf, String executorHandler) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo(groupId, jobDesc, scheduleConf, executorHandler);

        ReturnT<String> ret = commonCallXxlJob(JOB_INFO_ADD_URI, xxlJobInfo);
        // 如果code不为200, 则认为本次请求失败
        if (ReturnT.FAIL_CODE == ret.getCode()) {
            throw new BusinessException("xxl-job 添加定时任务异常: " + ret.getMsg());
        }
        // 如果返回内容为空, 也认为失败
        if (ret.getContent() == null) {
            throw new BusinessException("xxl-job 添加定时任务异常, 未返回任务ID");
        }
        return Integer.valueOf(ret.getContent());
    }

    public Integer addWarnJob(String jobDesc, Integer groupId, String scheduleConf, String executorHandler,String executorParam) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo(groupId, jobDesc, scheduleConf, executorHandler,executorParam);
        ReturnT<String> ret = commonCallXxlJob(JOB_INFO_ADD_URI, xxlJobInfo);
        // 如果code不为200, 则认为本次请求失败
        if (ReturnT.FAIL_CODE == ret.getCode()) {
            throw new BusinessException("xxl-job 添加定时任务异常: " + ret.getMsg());
        }
        // 如果返回内容为空, 也认为失败
        if (ret.getContent() == null) {
            throw new BusinessException("xxl-job 添加定时任务异常, 未返回任务ID");
        }
        return Integer.valueOf(ret.getContent());
    }



    /**
     * XXL-JOB 修改定时任务
     *
     * @param jobId           任务ID
     * @param jobDesc         任务描述
     * @param groupId         对应任务所在的任务组ID
     * @param scheduleConf    CRON表达式
     * @param executorHandler JobHandler
     */
    public void updateJob(Integer jobId, String jobDesc, Integer groupId, String scheduleConf, String executorHandler) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo(groupId, jobDesc, scheduleConf, executorHandler);
        xxlJobInfo.setId(jobId);
        ReturnT<Void> ret = commonCallXxlJob(JOB_INFO_UPDATE_URI, xxlJobInfo);
        // 如果code不为200, 则认为本次请求失败
        if (ReturnT.FAIL_CODE == ret.getCode()) {
            throw new BusinessException("xxl-job 修改定时任务异常: " + ret.getMsg());
        }
    }

    /**
     * XXL-JOB 启动定时任务
     *
     * @param jobId 任务ID
     */
    public void startJob(Integer jobId) {
        BaseJobInfo baseJobInfo = new BaseJobInfo();
        baseJobInfo.setId(jobId);
        ReturnT<Void> ret = commonCallXxlJob(JOB_INFO_START_URI, baseJobInfo);

        // 如果code不为200, 则认为本次请求失败
        if (ReturnT.FAIL_CODE == ret.getCode()) {
            throw new BusinessException("xxl-job 启动定时任务异常: " + ret.getMsg());
        }
    }

    /**
     * XXL-JOB 暂停定时任务
     *
     * @param jobId 任务ID
     */
    public void pauseJob(Integer jobId) {
        BaseJobInfo baseJobInfo = new BaseJobInfo();
        baseJobInfo.setId(jobId);
        ReturnT<Void> ret = commonCallXxlJob(JOB_INFO_STOP_URI, baseJobInfo);

        // 如果code不为200, 则认为本次请求失败
        if (ReturnT.FAIL_CODE == ret.getCode()) {
            throw new BusinessException("xxl-job 暂停定时任务异常: " + ret.getMsg());
        }
    }

    /**
     * XXL-JOB 删除定时任务
     *
     * @param jobId 任务ID
     */
    public void removeJob(Integer jobId) {
        BaseJobInfo baseJobInfo = new BaseJobInfo();
        baseJobInfo.setId(jobId);
        ReturnT<Void> ret = commonCallXxlJob(JOB_INFO_REMOVE_URI, baseJobInfo);

        // 如果code不为200, 则认为本次请求失败
        if (ReturnT.FAIL_CODE == ret.getCode()) {
            throw new BusinessException("xxl-job 删除定时任务异常: " + ret.getMsg());
        }
    }
}
