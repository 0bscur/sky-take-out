package com.sky.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
//import jdk.javadoc.internal.doclets.toolkit.util.ClassUseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sky.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */

    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        //判断为空
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);

        }
        //判断是否为新用户
        User user=userMapper.getByOpenid(openid);
        //如果是新用户，自动注册
        if(user==null){
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }
        //返回对象

        return user;
    }
    private String getOpenid(String code) {
        //调用微信接口服务获得openID
        Map<String,String> map=new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        String json=HttpClientUtil.doGet(WX_LOGIN,map);
        log.info("微信登录返回结果：{}", json);

        JSONObject jsonObject = JSONObject.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
