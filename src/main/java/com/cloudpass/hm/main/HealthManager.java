package com.cloudpass.hm.main;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import com.cloudpass.hm.api.AppStateServer;
import com.cloudpass.hm.api.EvacuatorServer;
import com.cloudpass.hm.api.TaskStateServer;
import com.cloudpass.hm.util.ConfigUtil;
import com.cloudpass.hm.util.SpringUtil;
import com.cloudpass.hm.zk.ProcessWatcher;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;

public class HealthManager {
	
	private static final Logger LOG = Logger.getLogger(HealthManager.class);
	
	private static RedisTemplate<String,Object> redisTemplate;
	
	private static Marathon marathon;
	
	private static String domain;
	
	private static String prefix;
	
	private static String routerMatch;
	
	private static Integer appStateTime;
	
	private static Integer taskTime;
	
	private static Integer evaluateTime;
	
	private static String zkPeers;
	
	private static ExecutorService executorService;

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

		TaskStateServer taskStateServer = new TaskStateServer(marathon, redisTemplate, routerMatch, taskTime);

		EvacuatorServer evacuatorServer = new EvacuatorServer(marathon, redisTemplate, routerMatch, evaluateTime);
		
		executorService.execute(appStateServer);
		executorService.execute(taskStateServer);
		executorService.execute(evacuatorServer);
	}
	

	public static void main(String[] args) throws IOException {
		
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
		
		final int id = (int) Math.round(Math.random()*100);
		
		zkPeers = ConfigUtil.getProValue("env.properties", "env.zk.urls");
				
		Future<?> future = executorService.submit(new ProcessWatcher(id, zkPeers));
		
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			LOG.fatal(e.getMessage(), e);
			executorService.shutdown();
		}
	}

}
