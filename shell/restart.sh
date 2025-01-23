#!/usr/bin/env bash

source ./common.sh

###### 请启动该脚本之前，确保JDK 1.8.0_202 已经安装 #####
###### 请执行： java -version  确保 JDK 版本的正确性#####

######################## 杀掉java进程 ########################
jps -ml | grep ${app_name} | grep ${server_port} | awk '{print "kill -9 " $1}' | sh
sleep 1

######################## jvm 配置 ########################
java_opts="-server -Xms1g -Xmx4g -Xmn512m -Xss4m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=6"
#java_opts="-server -Xms512m -Xmx2048m -Xmn256m -Xss4m -XX:MaxMetaspaceSize=256m -XX:SurvivorRatio=8"
java_opts="${java_opts} -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${debug_port}"
java_opts="${java_opts} -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled"
java_opts="${java_opts} -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80"
java_opts="${java_opts} -XX:CMSMaxAbortablePrecleanTime=5000 -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC"
java_opts="${java_opts} -XX:+PrintGCDateStamps -verbose:gc -XX:+PrintGCDetails -XX:-OmitStackTraceInFastThrow"
java_opts="${java_opts} -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -Xloggc:${log_root}/gc.log"
java_opts="${java_opts} -XX:ErrorFile=${log_root}/hs_err_pid%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${log_root}/java.hprof"

java_prop=" -Dsun.net.inetaddr.ttl=300 -Dsun.net.inetaddr.negative.ttl=300 "
### 关闭控制台日志输出 ###
java_prop="${java_prop} -Dclose-console-log-flag=yes "
java_prop="${java_prop} -Dserver.port=${server_port} -Dspring.profiles.active=${profile_name} -Djava.security.egd=file:/dev/./urandom "

######################## 启动 jvm ########################

nohup java  ${java_opts} ${java_prop} -jar ${jar_run_name} 2>&1 >start.log &



######################## 输出启动日志 ########################
tailStart

sleep 1