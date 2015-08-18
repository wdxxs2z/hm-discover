package com.cloudpass.hm.util;

import java.io.InputStream;
import java.util.Properties;

import com.cloudpass.hm.main.HealthManager;

public class ConfigUtil {
	
	public static String getProValue(String path,String key) {
		InputStream is;
		String propertieV = null;
		try {
			is = HealthManager.class.getClassLoader().getResourceAsStream(path);
			Properties p=new Properties();
			p.load(is);
			propertieV = p.getProperty(key);
			is.close();
		} catch (Exception e) {
			
			e.printStackTrace();
		}		
		return propertieV;
	}

}
