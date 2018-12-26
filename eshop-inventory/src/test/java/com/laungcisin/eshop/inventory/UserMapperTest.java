package com.laungcisin.eshop.inventory;

import com.laungcisin.eshop.inventory.enums.UserSexEnum;
import com.laungcisin.eshop.inventory.mapper.UserMapper;
import com.laungcisin.eshop.inventory.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testInsert() throws Exception {
        userMapper.insert(new User("aa", "a123456", UserSexEnum.MAN));
        userMapper.insert(new User("bb", "b123456", UserSexEnum.WOMAN));
        userMapper.insert(new User("cc", "b123456", UserSexEnum.WOMAN));

        Assert.assertEquals(3, userMapper.getAll().size());
    }

    @Test
    public void testQuery() throws Exception {
        List<User> users = userMapper.getAll();
        if (users == null || users.size() == 0) {
            System.out.println("is null");
        } else {
            System.out.println(users.toString());
        }
    }


    @Test
    public void getById() throws Exception {
        User user = userMapper.getById(1);
        if (user == null) {
            System.out.println("is null");
        } else {
            System.out.println(user.toString());
        }
    }

    @Test
    public void testUpdate() throws Exception {
        User user = userMapper.getOne(6l);
        System.out.println(user.toString());
        user.setNickName("neo");
        userMapper.update(user);
        Assert.assertTrue(("neo".equals(userMapper.getOne(6l).getNickName())));
    }

}