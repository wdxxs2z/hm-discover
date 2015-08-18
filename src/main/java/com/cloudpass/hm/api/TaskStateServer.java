package com.cloudpass.hm.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.cloudpass.hm.util.HMUtil;

import mesosphere.marathon.client.Marathon;

public class TaskStateServer implements Runnable {
	
	private RedisTemplate<String,Object> redisTemplate;
	
	private ListOperations<String, Object> operations;
	
	public TaskStateServer(Marathon marathon, RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.operations = redisTemplate.opsForList();
	}

	@Override
	public void run() {
		while(true){
			try {
				Set<String> keys = redisTemplate.keys("*");
				for (String key : keys) {
					List<Object> range = operations.range(key, 0, -1);
					if (range.size()!=0) {
						for (Object task : range) {
							int i = 0;
							String[] hostPort = task.toString().split(":");
							InetAddress remoteAddress = null;
							try {
								remoteAddress = InetAddress.getByName(hostPort[0]);
							} catch (UnknownHostException e) {
								
							}
							boolean reachable = HMUtil.isReachable(remoteAddress, Integer.parseInt(hostPort[1]), 500);
							if (reachable == false) {
								operations.remove(key, i, task);
							}
							i++;
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
