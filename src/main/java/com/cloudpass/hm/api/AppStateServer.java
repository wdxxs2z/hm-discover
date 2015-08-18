package com.cloudpass.hm.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.cloudpass.hm.util.HMUtil;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppTasksResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.Task;

public class AppStateServer implements Runnable{
	
	private Marathon marathon;
	
	private RedisTemplate<String,Object> redisTemplate;
	
	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private ListOperations<String, Object> operations;
	
	public AppStateServer(Marathon marathon, RedisTemplate<String,Object> redisTemplate) {
		this.marathon = marathon;
		this.redisTemplate = redisTemplate;
		this.operations = redisTemplate.opsForList();
	}

	@Override
	public void run() {
		while(true){
			try {
				GetAppsResponse apps = marathon.getApps();
				List<App> appList = apps.getApps();
				for(App app : appList) {
					GetAppTasksResponse tasksResponse = marathon.getAppTasks(app.getId());
					Collection<Task> tasks = tasksResponse.getTasks();
					String key = "/rs" + app.getId() + ".fakedocker.com";
					List<Object> range = operations.range(key, 0, -1);
					for (Task task : tasks) {
						InetAddress remoteAddress = null;
						try {
							remoteAddress = InetAddress.getByName(task.getHost().replace("/", ""));
						} catch (UnknownHostException e) {
							
						}
						boolean reachable = HMUtil.isReachable(remoteAddress, task.getPorts().iterator().next(), 500);
						if (reachable) {
							String value = task.getHost().replace("/", "") + ":" + task.getPorts().iterator().next();							
							if (!range.contains(value)) {
								operations.leftPush(key, value);
							}							
						}
					}
				}
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
