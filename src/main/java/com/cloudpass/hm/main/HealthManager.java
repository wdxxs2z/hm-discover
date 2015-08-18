package com.cloudpass.hm.main;

import org.springframework.data.redis.core.RedisTemplate;

import com.cloudpass.hm.api.AppStateServer;
import com.cloudpass.hm.api.EvacuatorServer;
import com.cloudpass.hm.api.TaskStateServer;
import com.cloudpass.hm.util.SpringUtil;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

public class HealthManager {
	
	private RedisTemplate<String,Object> redisTemplate;
	
	private Marathon marathon;

	@SuppressWarnings("unchecked")
	private void init() {
		SpringUtil.start();
		redisTemplate = (RedisTemplate<String, Object>) SpringUtil.getBean(RedisTemplate.class);
		try {
			marathon = MarathonClient.getInstance("http://192.168.172.150:8080");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void hmService(){
		init();
		AppStateServer appStateServer = new AppStateServer(marathon, redisTemplate);
		Thread appStateThread = new Thread(appStateServer, "appStateThread");
		TaskStateServer taskStateServer = new TaskStateServer(marathon, redisTemplate);
		Thread taskStateThread = new Thread(taskStateServer, "taskStateThread");
		EvacuatorServer evacuatorServer = new EvacuatorServer(marathon, redisTemplate);
		Thread evacuatorThread = new Thread(evacuatorServer, "evacuatorThread");
		
		appStateThread.start();
		taskStateThread.start();
		evacuatorThread.start();
	}

	public static void main(String[] args) {
		new HealthManager().hmService();		
	}

}
