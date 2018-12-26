package com.laungcisin.eshop.inventory.controller;

import com.laungcisin.eshop.inventory.mapper.UserMapper;
import com.laungcisin.eshop.inventory.model.User;
import com.laungcisin.eshop.inventory.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @RequestMapping("/getCachedUserInfo")
    public User getCachedUserInfo() {
        return userService.getCachedUserInfo();
    }

    @RequestMapping("/getUsers")
    public List<User> getUsers() {
        List<User> users = userMapper.getAll();
        return users;
    }

    @RequestMapping("/getUser")
    public User getUser(Long id) {
        User user = userMapper.getOne(id);
        return user;
    }

    @RequestMapping("/add")
    public void save(User user) {
        userMapper.insert(user);
    }

    @RequestMapping(value = "update")
    public void update(User user) {
        userMapper.update(user);
    }

    @RequestMapping(value = "/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        userMapper.delete(id);
    }


}