package com.linkcircle.system.service.impl;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.exception.BusinessException;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.redis.config.RedisUtil;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.common.LoginLogResultEnum;
import com.linkcircle.system.common.RedisKeyFormat;
import com.linkcircle.system.common.SystemLoginUserInfo;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.Corp;
import com.linkcircle.system.entity.SysLoginLog;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.mapstruct.SysUserMapStruct;
import com.linkcircle.system.service.*;
import com.linkcircle.system.util.JwtUtil;
import com.linkcircle.system.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
@Service
public class SysLoginServiceImpl implements SysLoginService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysLoginLogService sysLoginLogService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysRoleMenuService roleMenuService;
    @Autowired
    private SysUserMapStruct sysUserMapStruct;
    @Autowired
    private DefaultKaptcha defaultKaptcha;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CorpUserService corpUserService;
    @Value("${jwt.expireMinute:300}")
    private int defaultTokenExpireMinute;
    /**
     * 用户登陆
     */
    @Override
    public Result<SysLoginResDTO> login(SysLoginReqDTO sysLoginReqDto, String ip) {
        // 校验 图形验证码
        checkCaptcha(sysLoginReqDto);
        // 验证登录名
        SysUser sysUser = sysUserService.getByPhoneOrEmail(sysLoginReqDto.getPhoneOrEmail());
        if (sysUser == null) {
            return Result.error("手机号或邮箱不存在！");
        }
        // 验证账号状态
        if (sysUser.getDisabledFlag()) {
            saveLoginLog(sysUser, ip,   LoginLogResultEnum.LOGIN_FAIL);
            return Result.error("您的账号已被禁用,请联系管理员！");
        }
        // 解密前端加密的密码
        String requestPassword = PasswordUtil.decrypt(sysLoginReqDto.getPassword());
        // 密码错误
        if (!sysUser.getPassword().equals(PasswordUtil.getEncryptPwd(requestPassword))) {
            return  Result.error("登录名或密码错误！");
        }
        SystemLoginUserInfo systemLoginUserInfo = sysUserMapStruct.convert(sysUser);
        List<Corp> corpList = corpUserService.getCorpByUserId(sysUser.getId());
        SysLoginResDTO sysLoginResDto;
        if(corpList.size()==1){
            systemLoginUserInfo.setCorpId(corpList.get(0).getId());
            sysLoginResDto = getLoginResult(systemLoginUserInfo);
            sysLoginResDto.setSwitchCorp(false);
        }else{
            sysLoginResDto = new SysLoginResDTO();
            sysLoginResDto.setSwitchCorp(true);
            sysLoginResDto.setCorpList(corpList);
        }
        // 获取登录结果信息
        //保存登录记录
        saveLoginLog(sysUser, ip, LoginLogResultEnum.LOGIN_SUCCESS);
        String token = JwtUtil.sign(systemLoginUserInfo);
        redisUtil.set(RedisKeyFormat.getLoginTokenKey(token),token,defaultTokenExpireMinute*60);
        // 设置 token
        sysLoginResDto.setToken(token);
        return Result.ok(sysLoginResDto);
    }

    @Override
    public Result tokenVerify(String token) {
        if(StringUtils.isEmpty(token)){
            return Result.errorAuth("token为空");
        }
        JWT jwt = JWTUtil.parseToken(token);
        if(!jwt.setKey(JwtUtil.DEFAULT_SECRET.getBytes()).verify()) {
            return Result.errorAuth("无效的token");
        }
        if(!jwt.validate(0)) {
            return Result.errorAuth("token已过期，请重新登录");
        }
        return Result.ok();
    }

    @Override
    public Result<SysLoginResDTO> switchCorp(String corpId) {
        SystemLoginUserInfo systemLoginUserInfo = LoginUserInfoHolder.get();
        systemLoginUserInfo.setCorpId(corpId);
        String newToken = JwtUtil.sign(systemLoginUserInfo);
        String oldToken = JwtUtil.getToken();
        redisUtil.del(RedisKeyFormat.getLoginTokenKey(oldToken));
        redisUtil.set(RedisKeyFormat.getLoginTokenKey(newToken),newToken);
        SysLoginResDTO sysLoginResDto = getLoginResult(systemLoginUserInfo);
        sysLoginResDto.setToken(newToken);
        return Result.ok(sysLoginResDto);
    }

    @Override
    public Result<SysLoginResDTO> captchaLogin(SysCaptchaLoginReqDTO sysCaptchaLoginReqDto, String ip) {
        return null;
    }

    /**
     * 生成图形验证码
     * @return
     */
    @Override
    public SysCaptchaDTO generateCaptcha() {
        String captchaText = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(captchaText);

        String base64Code;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", os);
            base64Code = Base64Utils.encodeToString(os.toByteArray());
        } catch (Exception e) {
            log.error("generateCaptcha error:", e);
            throw new BusinessException("生成验证码错误");
        }
        String uuid = UUID.randomUUID().toString().replace("-", CommonConstant.STRING_EMPTY);
        SysCaptchaDTO sysCaptchaDto = new SysCaptchaDTO();
        sysCaptchaDto.setCaptchaUuid(uuid);
        sysCaptchaDto.setCaptchaBase64Image("data:image/png;base64," + base64Code);
        sysCaptchaDto.setExpireSeconds(CommonConstant.CAPTCHA_EXPIRE_SECOND);
        String redisCaptchaKey = RedisKeyFormat.getCaptchaKey(uuid);
        redisUtil.set(redisCaptchaKey, captchaText, CommonConstant.CAPTCHA_EXPIRE_SECOND);
        return sysCaptchaDto;
    }


    /**
     * 获取登录结果信息
     */
    @Override
    public SysLoginResDTO getLoginResult(SystemLoginUserInfo systemLoginUserInfo) {
        String corpId = systemLoginUserInfo.getCorpId();
        SysLoginResDTO sysLoginResDto = sysUserMapStruct.convert(systemLoginUserInfo);
        // 前端菜单和功能点清单
        List<SysRoleDTO> roleList = sysUserRoleService.getRoleByUserIdAndCorpId(systemLoginUserInfo.getId(),corpId);
        List<SysMenuDTO> menuAndPointsList = roleMenuService.getMenuList(roleList.stream().map(SysRoleDTO::getId).collect(Collectors.toList()));
        sysLoginResDto.setMenuList(menuAndPointsList);
        return sysLoginResDto;
    }
    /**
     * 登出
     */
    @Override
    public Result<String> logout(String ip) {
        SystemLoginUserInfo systemLoginUserInfo = LoginUserInfoHolder.get();
        //保存登出日志
        SysLoginLog loginEntity = SysLoginLog.builder()
                .userId(systemLoginUserInfo.getId())
                .username(systemLoginUserInfo.getUsername())
                .loginIp(ip)
                .loginResult(LoginLogResultEnum.LOGIN_OUT.getCode())
                .createTime(LocalDateTime.now())
                .build();
        sysLoginLogService.save(loginEntity);
        return Result.ok();
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(SysUser sysUser, String ip, LoginLogResultEnum result) {
        SysLoginLog loginEntity = SysLoginLog.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .loginIp(ip)
                .loginResult(result.getCode())
                .createTime(LocalDateTime.now())
                .build();
        sysLoginLogService.save(loginEntity);
    }

    /**
     * 校验图形验证码
     *
     */
    private void checkCaptcha(SysLoginReqDTO sysLoginReqDto) {
        String redisCaptchaKey = RedisKeyFormat.getCaptchaKey(sysLoginReqDto.getCaptchaUuid());
        String redisCaptchaCode = (String)redisUtil.get(redisCaptchaKey);
        if (StringUtils.isBlank(redisCaptchaCode)) {
            throw new BusinessException("验证码已过期，请刷新重试");
        }
        if (!Objects.equals(redisCaptchaCode, sysLoginReqDto.getCaptchaCode())) {
            throw new BusinessException("验证码错误，请输入正确的验证码");
        }
        // 删除已使用的验证码
        redisUtil.del(redisCaptchaKey);
    }
}
