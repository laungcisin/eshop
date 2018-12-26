package com.laungcisin.eshop.inventory.listener;

import com.laungcisin.eshop.inventory.thread.RequestProcessThreadPool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 系统初始化监听器
 */
public class InitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 初始化工作线程池和内存队列
        RequestProcessThreadPool.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("======================销毁===========================");
    }
}
