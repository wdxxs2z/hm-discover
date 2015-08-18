package com.cloudpass.hm.util;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtil {
	
	private static ClassPathXmlApplicationContext  ctx = null;
	
	public static void start(){
    	ctx = new ClassPathXmlApplicationContext("classpath:spring-redis.xml");
    	ctx.start();
    }
    public static Object getBean(String beanName){
         return ctx.getBean(beanName);
    }  
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object getBean(Class c){
        return ctx.getBean(c);
   } 

}
