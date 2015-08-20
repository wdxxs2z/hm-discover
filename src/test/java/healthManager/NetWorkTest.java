package healthManager;

import java.util.Collection;
import java.util.List;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.HealthCheckResults;
import mesosphere.marathon.client.model.v2.Task;
import mesosphere.marathon.client.utils.MarathonException;

public class NetWorkTest {
	
	public static void main(String[] args) {
//		try {
//			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//			ArrayList<NetworkInterface> list = Collections.list(interfaces);
//			for (NetworkInterface netLink : list) {
//				System.out.println(netLink);
//				Enumeration<InetAddress> inetAddresses = netLink.getInetAddresses();
//				ArrayList<InetAddress> ips = Collections.list(inetAddresses);
//				for (InetAddress ip : ips) {
//					System.out.println(ip.getHostAddress());
//				}
//			}
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
		String key = "/rs/docker-tomcat-0100.fakedocker.com";
		String appId = key.substring(key.lastIndexOf("/") + 1).split("\\.")[0];
		System.out.println(appId);
		
		MarathonClient client = new MarathonClient();
		Marathon instance = MarathonClient.getInstance("http://192.168.172.150:8080");
		try {
			GetAppResponse appResponse = instance.getApp(appId);
			System.out.println(appResponse);
			App app = appResponse.getApp();
			Collection<Task> tasks = app.getTasks();
			for (Task task : tasks) {
				Collection<HealthCheckResults> results = task.getHealthCheckResults();
				for (HealthCheckResults result : results) {
					System.out.println(task.getHost() + ":" + task.getPorts().iterator().next() +
							result.getTaskId() + " : " + result.getAlive());
				}
			}
		} catch (MarathonException e) {
			e.printStackTrace();
		}
		GetAppsResponse apps = instance.getApps();
		List<App> apps2 = apps.getApps();
		for (App app : apps2) {
			if (app.getContainer()!=null) {
				System.out.println(app);
			}else{
				System.out.println("empty");
			}
		}
	}

}
