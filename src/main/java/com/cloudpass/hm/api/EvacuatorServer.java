package com.cloudpass.hm.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppsResponse;

public class EvacuatorServer implements Runnable {
	
	private Marathon marathon;
	
	private RedisTemplate<String,Object> redisTemplate;
	
	private Integer evaluateTime;
	
	private String routerMatch;
	
	public EvacuatorServer(Marathon marathon, RedisTemplate<String, Object> redisTemplate, String routerMatch, Integer evaluateTime) {
		this.marathon = marathon;
		this.redisTemplate = redisTemplate;
		this.routerMatch = routerMatch;
		this.evaluateTime = evaluateTime;
	}

	@Override
	public void run() {
		while(true) {
			try {
				List<String> appList = new ArrayList<String>();
				GetAppsResponse appResponse = marathon.getApps();
				if (appResponse != null) {
					List<App> apps = appResponse.getApps();
					for (App app : apps) {
						if (app.getContainer()!=null) {
							if (app.getContainer().getDocker().getPortMappings()!=null) {
								appList.add(app.getId().replace("/", ""));
							}
						}						
					}
					Set<String> keys = redisTemplate.keys("*");
					for (String key : keys) {
						String appId = key.substring(key.lastIndexOf("/") + 1).split("\\.")[0];
						if (appList.contains(appId) == false) {
							redisTemplate.delete(key);
						}
					}
				}else{
					Set<String> keys = redisTemplate.keys(routerMatch);
					if (keys.isEmpty()) {
						
					}else{
						redisTemplate.delete(keys);
					}					
				}			
				Thread.sleep(evaluateTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
