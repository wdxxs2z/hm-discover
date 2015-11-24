package com.cloudpass.hm.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.HealthCheck;
import mesosphere.marathon.client.model.v2.HealthCheckResults;
import mesosphere.marathon.client.model.v2.Task;

public class TaskStateServer implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(TaskStateServer.class);
	
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
		if (LOG.isInfoEnabled()) {
			LOG.info("Start check alived app instances and remove the dead app instance.");
		}
		while(true){
			try {
				Set<String> keys = redisTemplate.keys(routerMatch);
				
				keys.stream().filter(key -> !key.isEmpty()).forEach(key -> {
					String appId = key.substring(key.lastIndexOf("/") + 1).split("\\.")[0];
					Map<String,Boolean> isTaskAliave = new HashMap<String,Boolean>();
					try {
						GetAppResponse appResponse = marathon.getApp(appId);
						App app = appResponse.getApp();
						
						Integer portIndex = null;
						Collection<HealthCheck> healthChecks = app.getHealthChecks();
						portIndex = healthChecks.stream()
								.filter(protocol -> protocol.getProtocol().equalsIgnoreCase("http") || protocol.getProtocol().equalsIgnoreCase("https"))
								.findFirst().get().getPortIndex();
						
						if (portIndex != null) {
							Collection<Task> tasks = app.getTasks();
							final Integer port = portIndex;
							tasks.stream().forEach(task -> {
								String hostport = task.getHost() + ":" +task.getPorts().toArray()[port];
								Collection<HealthCheckResults> results = task.getHealthCheckResults();
								Boolean isAlive = false;
								if (results != null) {
									isAlive = results.stream().allMatch(hr -> hr.getAlive());
									isTaskAliave.put(hostport, isAlive);
								}				
							});
						}		
					} catch (Exception e) {
						redisTemplate.delete(key);
						if (LOG.isInfoEnabled()){
							LOG.debug("Remove the crash key : " + key);
						}
						LOG.fatal(e.getMessage(), e);
					}
					
					List<Object> ranges = operations.range(key, 0, -1);
					if (ranges.size()!=0) {
						int i = 0;
						
						for (Object task : ranges) {
							
							if (isTaskAliave.containsKey(task)) {
								if (isTaskAliave.get(task) == false) {
									operations.remove(key, i, task);
									if (LOG.isDebugEnabled()) {
										LOG.debug("Remove task: " + task);
									}
								}
							}else{
								operations.remove(key, i, task);
								if (LOG.isDebugEnabled()) {
									LOG.debug("Remove task: " + task);
								}
							}							
							i++;
						}
					}
					
				});
				Thread.sleep(taskTime);
			} catch (InterruptedException e) {
				LOG.fatal(e.getMessage(), e);
			}
		}
	}
}
