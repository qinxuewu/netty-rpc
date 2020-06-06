package com.github;
import com.github.server.RpcService;

/**
 * 〈〉
 *
 * @author qinxuewu
 * @create 20/6/6上午11:32
 * @since 1.0.0
 */

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        System.out.println("HelloServiceImpl----------------------------被调用");
        return "Hello! " + name;
    }
}