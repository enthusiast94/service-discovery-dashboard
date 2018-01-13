package com.manasb;

import com.manasb.servicediscovery.api.domain.ServiceConnection;
import com.manasb.servicediscovery.api.ZookeeperServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.Collection;

public class Main {

    public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181",
                new RetryOneTime(2000));
        curatorFramework.start();
        curatorFramework.blockUntilConnected();
        ZookeeperServiceRegistry registry = new ZookeeperServiceRegistry(curatorFramework, "/services", "serviceLinks");
        Closeable closeable = registry.registerServiceInstance("OMS", getAddress(), 8000);

        ServiceConnection serviceConnection = registry.getServiceConnection("MainMethod", "OMS");

        closeable.close();
        serviceConnection.close();
        curatorFramework.close();
    }

    private static String getAddress() throws Exception {
        Collection<InetAddress> ips = ServiceInstanceBuilder.getAllLocalIPs();
        if (ips.size() > 0) {
            return ips.iterator().next().getHostAddress();
        }

        throw new Exception("Failed to find address");
    }
}
