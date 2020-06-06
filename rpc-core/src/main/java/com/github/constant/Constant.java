package com.github.constant;

/**
 * 常量定义
 *
 * @author qinxuewu
 * @create 20/6/6上午10:10
 * @since 1.0.0
 */


public class Constant {
    /**
     * 默认超时时间
     */
    public static final int ZK_SESSION_TIMEOUT = 12000;
    /**
     * 注册地址的根节点目录
     */
    public static final String ZK_REGISTRY_ROOT_PATH = "/registry";
    /**
     * 数据节点目录
     */
    public static final String ZK_DATA_PATH = ZK_REGISTRY_ROOT_PATH + "/data";
}
