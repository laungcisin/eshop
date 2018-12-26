package com.laungcisin.eshop.cache;

import com.laungcisin.eshop.cache.listener.InitListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@SpringBootApplication
@MapperScan("com.laungcisin.eshop.cache.mapper")
public class CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }

    @Bean
    public JedisCluster JedisClusterFactory() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        jedisClusterNodes.add(new HostAndPort("192.168.33.61", 7001));
        jedisClusterNodes.add(new HostAndPort("192.168.33.61", 7002));
        jedisClusterNodes.add(new HostAndPort("192.168.33.61", 7003));
        jedisClusterNodes.add(new HostAndPort("192.168.33.61", 7004));
        jedisClusterNodes.add(new HostAndPort("192.168.34.61", 7005));
        jedisClusterNodes.add(new HostAndPort("192.168.34.61", 7006));
        JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes);

        return jedisCluster;
    }

    /**
     * 注册监听器
     *
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean servletListenerRegistrationBean() {
        ServletListenerRegistrationBean servletListenerRegistrationBean = new ServletListenerRegistrationBean();
        servletListenerRegistrationBean.setListener(new InitListener());
        return servletListenerRegistrationBean;
    }

}
