package com.cloudpass.hm.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class HMUtil {
	
	public static boolean isReachable(InetAddress localInetAddr, InetAddress remoteInetAddr, 
			int port, int timeout) {
		Boolean booleanisReachable = false;
		Socket socket = null;
		try {
			socket = new Socket();
			SocketAddress localSocketAddress = new InetSocketAddress(localInetAddr, 0);
			socket.bind(localSocketAddress);
			InetSocketAddress endpointAddress = new InetSocketAddress(remoteInetAddr, port);
			socket.connect(endpointAddress, timeout);
			booleanisReachable = true;
		} catch (Exception e) {
			
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Error occurred while closing socket..");
				}
			}
		}		
		return booleanisReachable;
	}
	
	public static boolean isReachable(InetAddress remoteInetAddr, 
			int port, int timeout){
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getByName("192.168.172.1");
		} catch (UnknownHostException e) {
		}
		return isReachable(localhost, remoteInetAddr, port, timeout);
	}
}
