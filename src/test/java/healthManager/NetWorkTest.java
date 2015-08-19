package healthManager;

import java.util.List;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppsResponse;

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
		MarathonClient client = new MarathonClient();
		Marathon instance = MarathonClient.getInstance("http://192.168.172.150:8080");
		GetAppsResponse apps = instance.getApps();
		List<App> apps2 = apps.getApps();
		for (App app : apps2) {
			if (app.getContainer()!=null) {
				System.out.println(app.getContainer().getDocker().getPortMappings());
			}else{
				System.out.println("empty");
			}
		}
	}

}
