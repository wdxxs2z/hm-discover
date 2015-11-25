package com.cloudpass.hm.zk;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.springframework.boot.SpringApplication;

import com.cloudpass.hm.controller.MarathonEventBusController;
import com.cloudpass.hm.main.HealthManager;

public class ProcessWatcher implements Runnable{
	
	private static final Logger LOG = Logger.getLogger(ProcessWatcher.class); 
	
	private static final String ROOT_NODE = "/marathon_hm_root";
	
	private static final String NODE_PREFIX = "/hm_";
	
	private int id;
	private ZookeeperService zkService;
	
	private String processNodePath;
	private String watchedNodePath;

	public ProcessWatcher(final int id, final String zkUrl) throws IOException {
		this.id = id;
		zkService = new ZookeeperService(zkUrl, new ProcessNodeWatcher());
	}



	@Override
	public void run() {
		if(LOG.isInfoEnabled()) {
			LOG.info("Process with id: " + id + " has started!");
		}
		
		final String rootNodePath = zkService.createNode(ROOT_NODE, false, false);
		if(rootNodePath == null) {
			throw new IllegalStateException("Unable to create/access leader election root node with path: " + ROOT_NODE);
		}
		
		processNodePath = zkService.createNode(rootNodePath + NODE_PREFIX, false, true);
		if(processNodePath == null) {
			throw new IllegalStateException("Unable to create/access process node with path: " + ROOT_NODE);
		}
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("[Process: " + id + "] Process node created with path: " + processNodePath);
		}

		attemptForLeaderPosition();
	}
	
	private void attemptForLeaderPosition() {
		
		final List<String> childNodePaths = zkService.getChildren(ROOT_NODE, false);
		
		Collections.sort(childNodePaths);
		
		int index = childNodePaths.indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));
		if(index == 0) {
			if(LOG.isInfoEnabled()) {
				LOG.info("[Process: " + id + "] I am the new leader!");
			}
			
			//start spring-boot and start pool searcher
			SpringApplication.run(MarathonEventBusController.class);
			
			new HealthManager().hmService();
		} else {
			final String watchedNodeShortPath = childNodePaths.get(index - 1);
			
			watchedNodePath = ROOT_NODE + "/" + watchedNodeShortPath;
			
			if(LOG.isInfoEnabled()) {
				LOG.info("[Process: " + id + "] - Setting watch on node with path: " + watchedNodePath);
			}
			zkService.watchNode(watchedNodePath, true);
		}
	}
	
	public class ProcessNodeWatcher implements Watcher{

		@Override
		public void process(WatchedEvent event) {
			if(LOG.isDebugEnabled()) {
				LOG.debug("[Process: " + id + "] Event received: " + event);
			}
			
			final EventType eventType = event.getType();
			if(EventType.NodeDeleted.equals(eventType)) {
				if(event.getPath().equalsIgnoreCase(watchedNodePath)) {
					attemptForLeaderPosition();
				}
			}
			
		}
	}

	

}
