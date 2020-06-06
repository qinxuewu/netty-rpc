package com.github.registry;

import com.github.constant.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现 客户端查询服务端调用地址
 *
 * @author qinxuewu
 * @create 20/6/6上午10:46
 * @since 1.0.0
 */


public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    /*注册地址*/
    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        /*连接zk服务*/
        ZooKeeper zk = connectServer();
        if(null != zk){
            /*监视zk节点*/
            watchNode(zk);
        }
    }

    /**
     * 服务发现
     * @return
     */
    public String discovery(){
        String data = null;
        int size = dataList.size();
        if(size > 0){
            if(size == 1){
                // 若只有一个地址，则获取该地址
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            }else {
                // 若存在多个地址，则随机获取一个地址
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;
    }

    /**
     * 连接zk服务
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            /*创建zk客户端*/
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
            LOGGER.info("zk 连接成功。。。。。。。。。");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("连接zk服务报错...", e);
        }
        return zk;
    }

    /**
     * 监视zk节点
     * @param zk
     */
    private void watchNode(final ZooKeeper zk){
        try {

            // 获取registry下的所有子节点，并设置监听，如果父节点下 的子节点状态改变了 则回调process
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_ROOT_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                        // 递归实现循环监听
                        watchNode(zk);
                    }
                }
            });
            // 获取 注册中心的所有地址
            LOGGER.debug(" 获取 注册中心的所有地址: {}", nodeList.size());

            // 所有父父节点下的注册地址.保存到集合中
            List<String> dataList = new ArrayList<>();
            byte[] bytes;
            for (String node : nodeList){
                // 循环获取子节点的值保存到集合中
                bytes = zk.getData(Constant.ZK_REGISTRY_ROOT_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("监视zk节点异常...", e);
        }
    }
}
