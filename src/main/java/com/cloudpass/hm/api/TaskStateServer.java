package com.cloudpass.hm.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.HealthCheck;
import mesosphere.marathon.client.model.v2.HealthCheckResults;
import mesosphere.marathon.client.model.v2.Task;
import mesosphere.marathon.client.utils.MarathonException;

public class TaskStateServer implements Runnable {
	
	private RedisTemplate<String,Object> redisTemplate;
	
	private ListOperations<String, Object> operations;
	
	private String routerMatch;
	
	private Integer taskTime;
	
	private Marathon marathon;
	
	public TaskStateServer(Marathon marathon, RedisTemplate<String, Object> redisTemplate, String routerMatch, Integer taskTime) {
		this.redisTemplate = redisTemplate;
		this.operations = redisTemplate.opsForList();
		this.routerMatch = routerMatch;
		this.taskTime = taskTime;
		this.marathon = marathon;
	}

	@Override
	public void run() {
		while(true){
			try {
				Set<String> keys = redisTemplate.keys(routerMatch);
				for (String key : keys) {
					
					String appId = key.substring(key.lastIndexOf("/") + 1).split("\\.")[0];
					Map<String,Boolean> isTaskAliave = new HashMap<String,Boolean>();
					try {
						GetAppResponse appResponse = marathon.getApp(appId);
						App app = appResponse.getApp();
						
						//add health check only http mode can add the routers.
						Integer portIndex = null;
						Collection<HealthCheck> healthChecks = app.getHealthChecks();
						Iterator<HealthCheck> checks = healthChecks.iterator();
						while (checks.hasNext()) {
							HealthCheck check = checks.next();
							if (check.getProtocol().equalsIgnoreCase("http") || check.getProtocol().equalsIgnoreCase("https")) {
								portIndex = check.getPortIndex();
								break;
							}
						}
						
						Iterator<Task> tasks = app.getTasks().iterator();
						while(tasks.hasNext()) {
							Task appTask = tasks.next();
							String hostport = appTask.getHost() + ":" +appTask.getPorts().toArray()[portIndex];
							Collection<HealthCheckResults> results = appTask.getHealthCheckResults();
							Boolean isAlive = false;
							if (results != null) {
								isAlive = appTask.getHealthCheckResults().iterator().next().getAlive();
							}
							isTaskAliave.put(hostport, isAlive);
						}
					} catch (MarathonException e) {
						redisTemplate.delete(key);
					}
					
					List<Object> range = operations.range(key, 0, -1);
					
					if (range.size()!=0) {
						
						int i = 0;
						
						for (Object task : range) {
							
							if (isTaskAliave.containsKey(task)) {
								if (isTaskAliave.get(task) == false) {
									operations.remove(key, i, task);
								}
							}else{
								operations.remove(key, i, task);
							}
							
							i++;
						}
					}
				}
				Thread.sleep(taskTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
