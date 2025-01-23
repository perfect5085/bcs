#!/usr/bin/env bash

###### 请启动该脚本之前，确保JDK 1.8.0_202 已经安装 #####
###### 请执行： java -version  确保 JDK 版本的正确性#####

######################## 定义变量 ########################
export app_name="bcs"
### 应用端口
export server_port=9090
### 启动环境（profile）
export profile_name="开发"

### 后端项目
export back_project_name="bcs"
export back_project_url="git@github.com:perfect5085/bcs.git"
export back_project_branch="main"


### 应用部署运行路径
export app_run_path="$(cd "$(dirname "$0")";pwd)"
### 源代码路径
export app_source_path="${app_run_path}/source"
### 调试端口
export debug_port=`expr ${server_port} + 5`
### 当前时间戳
export current_time="$(date "+%Y-%m-%d_%H:%M:%S")"
### 启动 jar 包原始名称
export jar_name="${app_name}-web.jar"
### 启动 jar 包运行名称
export jar_run_name="${app_name}-web-${profile_name}-${server_port}.jar"
### 日志路径
export log_root="${app_run_path}/log/"
######################## 定义变量 ########################

######################## 输出启动日志 ########################
function tailStart() {
  OD_IFS="$IFS"
  IFS=
  tail -f start.log | while read -r line; do

    printf '%s\n' "$line"

    is_success=$(echo $line | grep "Spring_Boot_Success" | wc -l)
    if [ $is_success -eq 1 ]; then
      ### 检查系统是否正常
      echo "check the system : [ curl http://127.0.0.1:${server_port}/open/status/ok.json ]"
      curl "http://127.0.0.1:${server_port}/open/status/ok.json"
      echo
      ### 退出 tail
      ps aux | grep "tail" | grep "start.log" | grep -v "grep" | awk '{print "kill -9 " $2}' | sh
      break
    fi

    is_fail=$(echo $line | grep "APPLICATION FAILED TO START" | wc -l)
    if [ $is_fail -eq 1 ]; then
      echo "FAILED!!!!FAILED!!!!FAILED!!!!"
      ### 退出 tail
      ps aux | grep "tail" | grep "start.log" | grep -v "grep" | awk '{print "kill -9 " $2}' | sh
      break
    fi
  done
  IFS="$OD_IFS"
}


######################## 检测网络链接 ########################
function checkNetwork() {

    #超时时间1秒
    timeout=1

    #目标网站
    target=www.baidu.com

    #获取响应状态码
    ret_code=`curl -I -s --connect-timeout $timeout $target -w %{http_code} | tail -n1`

    if [ "x$ret_code" = "x200" ]; then
        return 1
    else
        return 0
    fi
}