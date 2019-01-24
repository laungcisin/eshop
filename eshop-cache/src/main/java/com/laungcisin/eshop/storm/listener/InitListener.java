package com.laungcisin.eshop.storm.listener;

import com.laungcisin.eshop.storm.kafka.KafkaConsumer;
import com.laungcisin.eshop.storm.prewarm.CachePreWarmThread;
import com.laungcisin.eshop.storm.rebuild.RebuildCacheQueue;
import com.laungcisin.eshop.storm.rebuild.RebuildCacheThread;
import com.laungcisin.eshop.storm.spring.SpringContext;
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
