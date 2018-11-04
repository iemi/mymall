package com.mymall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * 加载配置文件，获取配置文件，配置项信息工具类
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private  static Properties properties;

    //代码执行顺序 静态代码块->普通代码块->构造代码块
    static {
        String fileName = "mmall.properties";
        properties = new Properties();
        try {
            //在classpath路径下加载该配置文件
            properties.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("读取配置文件异常",e);
        }
    }

    //根据key 获取value
    public static String getProperty(String key){
        String value = properties.getProperty(key);
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value;
    }


    //根据key 获取value 找不到传回默认值
    public static String getProperty(String key,String defaultValue){
        String value = properties.getProperty(key);
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value;
    }

}
