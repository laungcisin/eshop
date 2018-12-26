package com.laungcisin.eshop.inventory.model;


import com.laungcisin.eshop.inventory.enums.UserSexEnum;

import java.io.Serializable;


public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String password;
    private UserSexEnum userSex;
    private String nickName;

    public User() {
        super();
    }

    public User(String username, String password, UserSexEnum userSex) {
        super();
        this.password = password;
        this.username = username;
        this.userSex = userSex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserSexEnum getUserSex() {
        return userSex;
    }

    public void setUserSex(UserSexEnum userSex) {
        this.userSex = userSex;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "username " + this.username + ", pasword " + this.password + "sex " + userSex.name();
    }

}