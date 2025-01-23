#!/usr/bin/env bash

source ./common.sh

###### 请启动该脚本之前，确保JDK 1.8.0_202 已经安装 #####
###### 请执行： java -version  确保 JDK 版本的正确性#####

######################## 获取最新代码，编译，打包 ########################

sh bak-copy.sh

sh restart.sh