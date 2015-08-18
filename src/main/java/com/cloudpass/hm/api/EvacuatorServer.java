package com.cloudpass.hm.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;

public class EvacuatorServer implements Runnable {
	
	private Marathon marathon;
	
	private RedisTemplate<String,Object> redisTemplate;
	
	public EvacuatorServer(Marathon marathon, RedisTemplate<String, Object> redisTemplate) {
		this.marathon = marathon;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void run() {
		while(true) {
			try {
				List<String> appList = new ArrayList<String>();
				List<App> apps = marathon.getApps().getApps();
				for (App app : apps) {
					appList.add(app.getId().replace("/", ""));
				}
				Set<String> keys = redisTemplate.keys("*");
				for (String key : keys) {
					String appId = key.substring(key.lastIndexOf("/") + 1).split("\\.")[0];
					if (appList.contains(appId) == false) {
						redisTemplate.delete(key);
					}
				}
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
