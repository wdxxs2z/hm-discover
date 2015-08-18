package com.cloudpass.hm.main;

import org.springframework.data.redis.core.RedisTemplate;

import com.cloudpass.hm.api.AppStateServer;
import com.cloudpass.hm.api.EvacuatorServer;
import com.cloudpass.hm.api.TaskStateServer;
import com.cloudpass.hm.util.ConfigUtil;
import com.cloudpass.hm.util.SpringUtil;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

public class HealthManager {
	
	private RedisTemplate<String,Object> redisTemplate;
	
	private Marathon marathon;
	
	private String domain;
	
	private String prefix;
	
	private String routerMatch;
	
	private Integer appStateTime;
	
	private Integer taskTime;
	
	private Integer evaluateTime;

	@SuppressWarnings("unchecked")
	private void init() {
		SpringUtil.start();
		redisTemplate = (RedisTemplate<String, Object>) SpringUtil.getBean(RedisTemplate.class);
		try {
			String marathonEndpoint = ConfigUtil.getProValue("env.properties", "env.marathon.endpoint");
			marathon = MarathonClient.getInstance(marathonEndpoint);
			domain = ConfigUtil.getProValue("env.properties", "env.marathon.domain");
			prefix = ConfigUtil.getProValue("env.properties", "env.marathon.prefix");
			routerMatch = ConfigUtil.getProValue("env.properties", "env.marathon.routerMatch");
			appStateTime = Integer.parseInt(ConfigUtil.getProValue("env.properties", "env.marathon.appStateTime"));
			taskTime = Integer.parseInt(ConfigUtil.getProValue("env.properties", "env.marathon.taskTime"));
			evaluateTime = Integer.parseInt(ConfigUtil.getProValue("env.properties", "env.marathon.evaluateTime"));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void hmService(){
		init();
		AppStateServer appStateServer = new AppStateServer(marathon, redisTemplate, appStateTime, prefix, domain);
		Thread appStateThread = new Thread(appStateServer, "appStateThread");
		TaskStateServer taskStateServer = new TaskStateServer(marathon, redisTemplate, routerMatch, taskTime);
		Thread taskStateThread = new Thread(taskStateServer, "taskStateThread");
		EvacuatorServer evacuatorServer = new EvacuatorServer(marathon, redisTemplate, routerMatch, evaluateTime);
		Thread evacuatorThread = new Thread(evacuatorServer, "evacuatorThread");
		
		appStateThread.start();
		taskStateThread.start();
		evacuatorThread.start();
	}

	public static void main(String[] args) {
		new HealthManager().hmService();		
	}

}
