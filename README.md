# hm-discover
This project is depend on marathon and gorouter

> NOTE: 还没有实现组建高可用的部分，后续将继续完善
> gorouter的版本是修改过的，采用redis做为其服务发现路由表

# 安装单机版的mesos + docker + marathon + zookeeper

<pre>
0.zookeeper 启动ZK
zkServer.sh start
1.mesos master 启动Mesos
mesos-master --work_dir=/var/lib/mesos --zk=zk://192.168.172.150:2181/mesos --quorum=1
2.master slaver 如果有启动脚本，最好在mesos的slaver上声明一下IP的hostname
mesos-slave --master=zk://192.168.172.150:2181/mesos --containerizers=docker,mesos --executor_registration_timeout=5mins
3.marathon 启动Marathon
/home/jojo/marathon-0.9.0/bin/start --master zk://192.168.172.150:2181/mesos --zk zk://192.168.172.150:2181/marathon
4.redis 启动Redis
redis-server redis.conf
5.gorouter 开启gorouter
gorouter -c ./example_config/example.yml &
</pre>
