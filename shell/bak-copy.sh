#!/usr/bin/env bash

source ./common.sh

###### 请启动该脚本之前，确保JDK 1.8.0_202 已经安装 #####
###### 请执行： java -version  确保 JDK 版本的正确性#####

######################## 备份、拷贝最新包 ########################
cd ${app_run_path}

### 备份运行的Jar
if [ -f "${jar_run_name}" ]; then
  mv ${jar_run_name} "${jar_run_name}.${current_time}"
fi

### 从源码编译输出中拷贝Jar
if [ -d "${app_source_path}" ]; then
  cp ${app_source_path}/${back_project_name}/web/target/${jar_name} ${jar_name}
  cp ${jar_name} ${jar_run_name}

### 否则，从当前目录中查找Jar包
elif [ -f "${jar_name}" ]; then
  cp ${jar_name}  ${jar_run_name}

else
  echo "找不到Jar包：${jar_name}"
fi

### 删除很久之前的部署包
# 设置保留备份文件的最大个数，其余的备份文件删除掉，默认保留最近的5份
max=5
count=`ls -lrt ${app_run_path} | awk  '/'"${app_name}-web-"'/ {print $9}' | wc -l`
if [ $count -gt $max ];then
    echo ""
    del=$[$count-$max]
    file=`ls -lrt ${app_run_path} | awk  '/'"${app_name}-web-"'/ {print $9}'  | head -${del}`
    echo "删除备份文件：${file}"
    rm ${file}
fi
