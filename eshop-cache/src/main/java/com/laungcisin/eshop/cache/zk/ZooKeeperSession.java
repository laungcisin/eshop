package com.laungcisin.eshop.cache.zk;

import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

public class ZooKeeperSession {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private ZooKeeper zookeeper;

    private ZooKeeperSession() {
        // 去连接zookeeper server，创建会话的时候，是异步去进行的
        // 所以要给一个监听器，说告诉我们什么时候才是真正完成了跟zk server的连接
        try {
            this.zookeeper = new ZooKeeper(
                    "192.168.33.61:2181,192.168.33.62:2181,192.168.33.63:2181",
                    50000,
                    new ZooKeeperWatcher());

            // 给一个状态CONNECTING，连接中
            System.out.println(zookeeper.getState());

            try {
                // CountDownLatch
                // java多线程并发同步的一个工具类
                // 会传递进去一些数字，比如说1,2 ，3 都可以
                // 然后await()，如果数字不是0，那么就卡住，等待

                // 其他的线程可以调用countDown()，减1
                // 如果数字减到0，那么之前所有在await的线程，都会逃出阻塞的状态
                // 继续向下运行

                connectedSemaphore.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("ZooKeeper session established......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分布式锁
     *
     * @param productId 商品id
     */
    public void acquireDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;

        try {
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("success to acquire lock for product[id=" + productId + "]");
        } catch (Exception e) {
            // 如果那个商品对应的锁node，已经存在了，就是已经被别人加锁了，那么就这里就会报错：NodeExistsException
            int count = 0;
            while (true) {
                try {
                    //FIXME:休眠时间可修改
                    Thread.sleep(1000);
                    zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e2) {
                    count++;
                    System.out.println("the " + count + " times try to acquire lock for product[id=" + productId + "]......");
                    continue;
                }
                System.out.println("success to acquire lock for product[id=" + productId + "] after " + count + " times try......");
                break;
            }
        }
    }

    /**
     * 释放掉一个分布式锁
     *
     * @param productId 商品id
     */
    public void releaseDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;
        try {
            zookeeper.delete(path, -1);
            System.out.println("释放zk分布式锁");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ZooKeeperWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("Receive watched event: " + event.getState());
            if (Event.KeeperState.SyncConnected == event.getState()) {
                connectedSemaphore.countDown();
            }
        }
    }

    /**
     * 获取单例
     */
    public static ZooKeeperSession getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 封装单例的静态内部类
     */
    private static class Singleton {
        private static ZooKeeperSession instance;

        static {
            instance = new ZooKeeperSession();
        }

        private static ZooKeeperSession getInstance() {
            return instance;
        }
    }

}
