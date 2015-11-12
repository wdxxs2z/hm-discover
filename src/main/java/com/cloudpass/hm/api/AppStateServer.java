package com.cloudpass.hm.api;

import java.util.Collection;
import java.util.List;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppTasksResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.HealthCheck;
import mesosphere.marathon.client.model.v2.Task;

public class AppStateServer implements Runnable{
	
	private Marathon marathon;
	
	private RedisTemplate<String,Object> redisTemplate;
	
	private Integer appStateTime;
	
	private String prefix;
	
	private String domain;
	
	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private ListOperations<String, Object> operations;
	
	public AppStateServer(Marathon marathon, RedisTemplate<String,Object> redisTemplate, Integer appStateTime, String prefix, String domain) {
		this.marathon = marathon;
		this.redisTemplate = redisTemplate;
		this.operations = redisTemplate.opsForList();
		this.appStateTime = appStateTime;
		this.prefix = prefix;
		this.domain = domain;
	}

	@Override
	public void run() {
		while(true){
			try {
				GetAppsResponse apps = marathon.getApps();
				List<App> appList = apps.getApps();
				for(App app : appList) {
					if (app.getContainer()!=null) {
						if (app.getContainer().getDocker().getPortMappings()!=null) {
							GetAppTasksResponse tasksResponse = marathon.getAppTasks(app.getId());
							Collection<Task> tasks = tasksResponse.getTasks();
							String key = prefix + app.getId() + domain;
							List<Object> range = operations.range(key, 0, -1);
							
							//Add Health Check
							Integer portIndex = null;
							Collection<HealthCheck> healthChecks = app.getHealthChecks();
							
							portIndex = healthChecks.stream()
							.filter(protocol -> protocol.getProtocol().equalsIgnoreCase("http") || protocol.getProtocol().equalsIgnoreCase("https"))
							.findFirst().get().getPortIndex();
							
							if (portIndex != null) {
								final Integer port = portIndex;
								tasks.stream().filter(hasNullHealth -> hasNullHealth.getHealthCheckResults() != null)
								.filter(task -> task.getHealthCheckResults().size() > 0)
								.filter(task -> task.getHealthCheckResults().size() == task.getHealthCheckResults().stream().filter(checkResult -> checkResult.getAlive()).count())
								.forEach(aliveTask -> {
									if (aliveTask != null) {
										String value = aliveTask.getHost().replace("/", "") + ":" + aliveTask.getPorts().toArray()[port];
										if (!range.contains(value)) {
											operations.leftPush(key, value);
										}
									}						
								});
							}
						}
					}				
				}
				Thread.sleep(appStateTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
