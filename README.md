# NekoGameServer

## **Java后端雏形**  
***
### **项目简要说明**  
1. 为回合制/半回合制等适合 状态同步 的游戏所做的Java后端
2. 部分代码有参考以及引用轮子
3. 修修补补又能穿几年的类型
4. 支持高并发,集群,无缝切换

#### **涉及的组件、中间件、技术栈** 

1. SpringCloud全家桶
2. Redis、MongoDB、Zookeeper、Kafka、Nacos
3. 手把手从0开始到0.1的Netty通信,自定义封包,Protobuff和JSON性能对比例子
4. Gateway和集群,无感知切换处理端
5. 配置中心与AutoConfiguration的坑与沥青
6. 人数上限较高的多人战下(>=30 , ~ <=120),关于kafka等相应的处理
7. Guava的限流器的运用
8. CaffeineCacheManager、Redis等构成三级缓存的实际案例
9. 被绕晕的线程的运用和对应的轮子

***
## **Enviorment**

|    Software  |   Version   |   Summary   |
| :--- | ---- | ---- |
| JDK | 1.8 & 11 | None |
| MongoDB | lastest version | Standalone |
| Redis | 5.0 | Standalone |
| Zookeeper | lastest version | Use JDK1.8 & Standalone |
| Kafka | lastest version | Use JDK1.8 & Standalone |
| Nacos | &gt;= 2.0.2 | Standalone |
| Maven | lastest version | None |

**若Docker可用,测试时请尽量使用**

## **Project Structure**
***
### **项目使用 JDK 11**
***
| 模块名 | 主要作用 | 注意事项|
| :--- | ---- | ---- |
|game-common|公共引用的包,主要用于依赖和实体类定义||
|game-network-param|公共引用的参数类的包,主要用于网络参数||
|game-gateway-message-starter|主要用于gateway模块,以及所有使用中间件收发消息的模块||
|game-dao|数据库操作模块||
|game-client|测试用的客户端||
|game-web-gateway|网络服务中心的网关,连接服务中心前的处理||
|game-center|网络服务中心模块,处理HTTP请求,包括注册,创建角色账号等功能||
|game-gateway|游戏网关模块,经过game-center权鉴后所连接的模块||
|game-neko|猫猫服务器(不对)模块,处理所有非战斗中的用户逻辑,也包括建立战斗副本||
|game-raidbattle|战斗副本模块,处理加入战斗,战斗判定,战斗结束,奖励分发等逻辑||
|game-im|聊天模块,单服聊天,全服聊天等|全服聊天需要改进|
|game-log|日志模块,记录绝大部分玩家请求以及部分请求结果||
|game-sync-db|数据离线同步|无须启用|
|game-mq-system|Rocket MQ遗留|无须启用,不影响|


## **Prepare**

**安装Docker**

<a href="https://www.runoob.com/docker/centos-docker-install.html" target="_blank">Linux系统安装Docker</a>

<a href="https://www.runoob.com/docker/windows-docker-install.html" target="_blank">Windows系统安装Docker</a>
* Windows10系统还可以安装Wsl2进行Docker启动的提速,,请百度
* 注意,Wsl2可能会与安卓模拟器等有无法调和的冲突
  
基于Docker下配置Redis、Zookeeper、Kafka、Nacos

以Windodws10系统Docker为例

### Redis
* 进入redis-docker文件夹,打开终端,执行 `docker-compose up -d`
* 待拉取镜像后,redis以单机模式正常运行(默认AOF方式,默认密码nekoroot)
* 数据存储在./redis中
* **必须设置一个密码**

### Zookeeper + Kafka
* 进入kafka-docker文件夹,打开终端,执行 `docker-compose up -d`
* 待拉取镜像后,zookeeper和kafak以单机模式正常运行
* 数据存储在./kafka-data中
* 如出现后续出现kafka启动失败,是因为Windows系统和Kafka之间的问题,可以**手动删除**./kafka-data/kafka-logs的所有数据

### Nacos
* [GitHub地址](https://github.com/nacos-group/nacos-docker)
* 安装使用的是国内加速地址,步骤如下
* `git clone --depth 1 https://hub.fastgit.org/nacos-group/nacos-docker.git`
* `cd nacos-docker`
* `docker-compose -f example/standalone-derby.yaml up`
* 注意,Windows系统不会加载.env也不会生效,需要手动编辑 example/standalone-derby.yaml
* 把  `image: nacos/nacos-server:${XXXXXX}` 改为 `image: nacos/nacos-server:2.0.2`
* 也可以手动拉取对应的镜像,再自行更改版本号
* nacos以单机模式正常运行
* http://127.0.0.1:8848/nacos 访问管理界面,用户名密码都是 nacos
* 进入管理界面后,选择左侧的命名空间,进行如下操作
* 1."新建命名空间",命名ID为 `b8142ed2-6e55-49f3-9e7a-ca83ed3679b6` ,空间名(如`Neko`)和描述随意
* 2.命名ID可以自行更改，但必须与各模块的`bootstrap.yaml`中记载的一致
* 3."配置管理"->"配置列表",上方的标签选择刚刚新建的空间名(例子`Neko`)后,选择导入配置
* 4.导入项目中`assets/nacos_config.zip`,可以看到与项目中的`basic.yaml`一样内容的配置文件在表格中
* 至此nacos配置完毕

### MongoDB
* 不使用Docker安装方法,直接百度对应的系统(Windows/Linux)即可安装 
* 建立用户, 用户名: `nekoroot` 密码: `nekoroot`
* 用户名和密码不包含任何空格
* **必须设置一个密码**

***
如果是非Docker用户,则按照正常流程安装对应的中间件即可  
仅 **Redis** 配置文件稍有改动  
其中,ZooKeeper和Kafka在启动时需要指定JDK 1.8  
***

## **Project Run**

以编辑器idea为例运行项目
载入项目后,等待Maven拉取相应的安装包,如出现超时是正常现象  
请百度 "Maven超时" 等获取相关解决办法  

在idea编辑器中,选择 "Maven"->"LifeCycle"->"install"运行 (install等期间出现问题请联系我)  
按以下顺序作为初次启动顺序:

1. game-web-gateway
2. game-center
3. game-gateway
4. game-neko
5. game-raidbattle
6. game-client  
  
在启动时可以加入 -Dlog4j.skipJansi=false 用于打印彩色Log

如果都正常启动,可以在Redis以及MongoDB中看到大量`"_DB"`后缀的表/行

正常启动后的测试流程
1. 切换到 game-client的模块的视窗 (一般是 "GameClientMain")  
可输入`login 账号名 密码` 注册账号
2. 初次注册后提示需要创建分区账号角色  
可输入`cp 角色名`
3. 再次输入 `login 刚才的账号 刚才的密码` 即可完成首次登陆
4. 输入`msg 201`到`msg 209`之间可以查询对应的消息
5. 输入`msg 501`可以抽卡
6. 输入`msg 401`可以创建战斗,返回一个 `RaidId` 的字符串和对应的数据,  
如缺少消耗的道具,输入`close`断开链接后可以在redis中更改
7. 创建战斗后输入msg 1001可以进行攻击
8. 另外启动一个game-client，按1-4操作,正常登陆后输入`join 刚才得到的RaidId字符串` 即可加入战斗
9. 输入`say 内容`可以和同一服务端的聊天,输入`speaker 内容`可以全服聊天
10. 详细指令和参数查看 `game-client` 模块中的 `GameClientCommand.class`
