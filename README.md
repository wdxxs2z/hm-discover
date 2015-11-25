# hm-discover
This project is depend on marathon and gorouter

> NOTE: 完成高可用，基于zookeeper的leader选举Master<br />
> gorouter的版本是修改过的，采用redis做为其服务发现路由表<br />

安装单机版的mesos + docker + marathon + zookeeper

#### 0.zookeeper 启动ZK
```
zkServer.sh start
```
#### 1.mesos master 启动Mesos
```
mesos-master --work_dir=/var/lib/mesos --zk=zk://192.168.172.150:2181/mesos --quorum=1
```
#### 2.master slaver 如果有启动脚本，最好在mesos的slaver上声明一下IP的hostname
```
mesos-slave --master=zk://192.168.172.150:2181/mesos --containerizers=docker,mesos --executor_registration_timeout=5mins
```
#### 3.marathon 启动Marathon
```
/home/jojo/marathon-0.9.0/bin/start --master zk://192.168.172.150:2181/mesos --zk zk://192.168.172.150:2181/marathon
```
#### 4.redis 启动Redis
```
redis-server redis.conf
```
#### 5.gorouter 开启gorouter
```
gorouter -c ./example_config/example.yml &
```
#### 6.hm-dis 开启服务发现 必须保证jdk8 编码使用lamda表达式
```
java -jar  & [ourjar.jar]
```
AppState 接收并检测存活的app,这里只包含容器化带端口的应用服务，其它的将被抛弃<br />
TaskState 循环检测redis中的节点是否存活，如果没有，则将其清除<br />
Evacuator 检测空的无效的应用，并在redis中清除<br />

只检测http或https的应用，至于link的数据库，则会依照相应的Marathon Healthcheck来定<br />

#### 功能补充
程序本身只轮询的检测marathon，但后期将完善根据事件注册回掉的机制写gorouter<br />
开启marathon的事件回调机制:
```
./start --master zk://192.168.49.128:2181/mesos --zk zk://192.168.49.128:2181/marathon --event_subscriber http_callback --http_endpoints http://192.168.49.1:8080/event_callback
```
MarathonEventBusController 处理具体的事件，由于事件比较零散，所以后续在做这部分功能的补充，目前只轮询和打印事件.
