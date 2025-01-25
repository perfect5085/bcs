# 使用 JDK 1.8 基础镜像
FROM openjdk:8-jdk-alpine
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/' /etc/apk/repositories && \
    apk update && \
    apk add --no-cache curl

# 设置工作目录
WORKDIR /app

# 复制应用程序 JAR 文件到容器
COPY bcs-web.jar bcs-web.jar

# 复制启动shell脚本
COPY bak-copy.sh bak-copy.sh
COPY common.sh common.sh
COPY deploy.sh deploy.sh
COPY restart.sh restart.sh
COPY tailf.sh tailf.sh

# 确保所有脚本具有执行权限
RUN chmod +x bak-copy.sh common.sh deploy.sh  restart.sh tailf.sh

# 暴露应用的端口
EXPOSE 9090

# 使用脚本作为容器启动命令
ENTRYPOINT ["sh", "deploy.sh"]