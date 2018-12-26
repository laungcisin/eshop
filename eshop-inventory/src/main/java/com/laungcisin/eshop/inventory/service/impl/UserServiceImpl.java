package com.laungcisin.eshop.inventory.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.laungcisin.eshop.inventory.dao.RedisDAO;
import com.laungcisin.eshop.inventory.model.User;
import com.laungcisin.eshop.inventory.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private RedisDAO redisDAO;

    @Override
    public User getCachedUserInfo() {
        redisDAO.set("cached_user", "{\"username\": \"zhangsan\", \"age\": 25, \"nickname\":\"zhangsan-nickname\"}");
        String json = redisDAO.get("cached_user");
        JSONObject jsonObject = JSONObject.parseObject(json);

        User user = new User();
        user.setUsername(jsonObject.getString("name"));
        user.setNickName(jsonObject.getString("nickname"));

        return user;
    }
}
