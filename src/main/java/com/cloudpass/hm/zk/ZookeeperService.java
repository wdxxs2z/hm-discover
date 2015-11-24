package com.cloudpass.hm.zk;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.cloudpass.hm.zk.ProcessWatcher.ProcessNodeWatcher;

public class ZookeeperService {
	
	private ZooKeeper zk;
	
	public ZookeeperService(final String zkUrl, ProcessNodeWatcher processNodeWatcher) throws IOException {
		zk = new ZooKeeper(zkUrl, 3000, processNodeWatcher);
	}

	public List<String> getChildren(String rootNode, boolean isWatch) {
		List<String> zkChild = null;
		try {
			zkChild = zk.getChildren(rootNode, isWatch);			
		} catch (KeeperException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
		return zkChild;
	}

	public boolean watchNode(String watchedNodePath, boolean isWatch) {
		boolean watched = false;
		try {
			Stat exists = zk.exists(watchedNodePath, isWatch);
			if (exists != null){
				watched = true;
			}
		} catch (KeeperException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
		return watched;
	}

	public String createNode(String node, boolean isWatch, boolean ephimeral) {
		String createNodePath = null;
		try {
			Stat stat = zk.exists(node, isWatch);
			if (stat == null) {
				createNodePath = zk.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE, (ephimeral ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
			}else{
				createNodePath = node;
			}
		} catch (KeeperException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
		return createNodePath;
	}

}
