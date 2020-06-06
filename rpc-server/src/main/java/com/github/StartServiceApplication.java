package com.github;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author qinxuewu
 * @create 20/6/6下午1:42
 * @since 1.0.0
 */


public class StartServiceApplication {

    public static void main(String[] args) {
        ApplicationContext application=new ClassPathXmlApplicationContext("application-server.xml");
        String[] beanDefinitionNames = application.getBeanDefinitionNames();
        for (String beanDefinitionName:beanDefinitionNames){
            System.out.println("bebeanDefinitionName: "+beanDefinitionName);
        }
    }
}
