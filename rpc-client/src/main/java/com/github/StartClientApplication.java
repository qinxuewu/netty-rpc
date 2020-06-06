package com.github;

import com.github.client.RpcProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 〈〉
 *
 * @author qinxuewu
 * @create 20/6/6下午1:45
 * @since 1.0.0
 */


public class StartClientApplication {

    public static void main(String[] args) {
        ApplicationContext application=new ClassPathXmlApplicationContext("application-client.xml");
        RpcProxy rpcProxy=application.getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("feizi");
        System.out.println("============>result: " + result);
    }
}
