package com.github.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * Rpc响应的modle
 *
 * @author qinxuewu
 * @create 20/6/6上午10:03
 * @since 1.0.0
 */


@Data
public class RpcRepsonse implements Serializable {
    /**
     *  响应ID
     */
    private String reponseId;
    /**
     * 异常对象
     */
    private Throwable error;
    /**
     * 响应结果
     */
    private Object result;


    public boolean hasError(){
        return error != null;
    }

}
