package com.laungcisin.eshop.inventory.mapper;


import com.laungcisin.eshop.inventory.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User getById(@Param("id") Integer id);

    List<User> getAll();

    User getOne(Long id);

    void insert(User user);

    void update(User user);

    void delete(Long id);

}