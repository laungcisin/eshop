package com.laungcisin.eshop.cache.listener;

import com.laungcisin.eshop.cache.kafka.KafkaConsumer;
import com.laungcisin.eshop.cache.rebuild.RebuildCacheQueue;
import com.laungcisin.eshop.cache.rebuild.RebuildCacheThread;
import com.laungcisin.eshop.cache.spring.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 系统初始化监听器
 */
public class InitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 初始化spring相关容器，方便获取ApplicationContext
        ServletContext sc = sce.getServletContext();
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sc);
        SpringContext.setApplicationContext(context);

        new Thread(new KafkaConsumer("cache-message")).start();
        new Thread(new RebuildCacheThread()).start();
        RebuildCacheQueue.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("======================销毁===========================");
    }
}
