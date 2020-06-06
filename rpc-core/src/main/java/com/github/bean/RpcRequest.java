package com.github.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Rpc请求的model
 *
 * @author qinxuewu
 * @create 20/6/6上午10:03
 * @since 1.0.0
 */


@Data
public class RpcRequest implements Serializable {

    /**
     *  请求ID
     */
    private String requestId;
    /**
     * 调用class类名
     */
    private String className;
    /**
     * 调用方法名
     */
    private String methodName;
    /**
     * 调用参数类型集合
     */
    private Class<?>[] parameterTypes;
    /**
     * 调用参数集合
     */
    private Object[] parameters;


}
