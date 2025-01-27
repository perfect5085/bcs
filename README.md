# BCS
BCS(Balance Calculation System)是一个实时余额计算系统。

# 系统功能
* 实时查询账户的余额。
* 对指定账户进行取款操作。
* 对指定账户进行存款操作。
* 将A账户的部分金额转移给B账户。
* 查询交易的状态。
* 重置超时交易

# 系统介绍
* 基于SpringBoot + Mybatis-plus + MySQL + Redis 的一个Web系统。

# 部署步骤
1. 应用打包编译【忽略测试】 ： mvn clean install -Dmaven.test.skip
2. 根据 Dockerfile【详见 doc/Dockerfile 】 ， 构建镜像，名称 bcs-web ： docker build -t bcs-web .
3. 登录阿里云镜像仓库【只需要登录一次】 ： docker login --username=at9951j6p@aliyun.com crpi-38ox03csjf2n9jik.cn-wulanchabu.personal.cr.aliyuncs.com
4. 将本地镜像打标签和远程仓库关联 ： docker tag bcs-web:latest crpi-38ox03csjf2n9jik.cn-wulanchabu.personal.cr.aliyuncs.com/perfectdocker/bcs:latest
5. 将本地镜像推送到远程仓库 ： docker push crpi-38ox03csjf2n9jik.cn-wulanchabu.personal.cr.aliyuncs.com/perfectdocker/bcs:latest
6. 在阿里云的ACK集群无状态应用进行重新部署。

# 系统部署架构
* 详见《系统部署架构》的Word文档。
* ACK 集群部署清单： doc/bcs-prod-deployment.yml
* LoadBalancer 详见： doc/bcs-prod-service.yml

# 系统设计
* 系统的数据库结构设计和接口设计详见《系统设计》word文档。

# 测试
## 功能测试
1. 使用Swagger生成的接口文档： http://8.130.178.124:9090/doc.html

## 性能测试
1. 使用JMeter对系统进行压测，详见《性能测试报告》的Word文档。

## 可靠性和可用性测试
1. 详见《可靠性和可用性测试》的Word文档。

## 集成测试
1. 详见《系统集成测试报告》的Word文档。
2. 生成测试覆盖率报告： 在源码的web目录下执行：  1,  mvn clean test   2,  mvn jacoco:report

# 系统二期【展望】
1. 详见《系统二期设计【展望】》的word文档。