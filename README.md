# BCS
BCS(Balance Calculation System)是一个实时余额计算系统。

# 系统功能
* 实时查询账户的余额。
* 对指定账户进行取款操作。
* 对指定账户进行存款操作。
* 将A账户的部分金额转移给B账户。

# 系统介绍
* 基于SpringBoot + PostgreSQL + Redis 的一个Web系统。

# 部署步骤
1. 应用打包编译【忽略测试】 ： mvn clean install -Dmaven.test.skip
2. 根据 Dockerfile ， 构建镜像，名称 bcs-web ： docker build -t bcs-web .
3. 登录阿里云镜像仓库【只需要登录一次】 ： docker login --username=at9951j6p@aliyun.com crpi-38ox03csjf2n9jik.cn-wulanchabu.personal.cr.aliyuncs.com
4. 将本地镜像打标签和远程仓库关联 ： docker tag bcs-web:latest crpi-38ox03csjf2n9jik.cn-wulanchabu.personal.cr.aliyuncs.com/perfectdocker/bcs:latest
5. 将本地镜像推送到远程仓库 ： docker push crpi-38ox03csjf2n9jik.cn-wulanchabu.personal.cr.aliyuncs.com/perfectdocker/bcs:latest
6. 在阿里云的ACK集群无状态应用进行重新部署。

# 系统部署架构
* 详见《系统部署架构》的Word文档。

# 测试
## 功能测试
1. 系统已经自动根据Swagger生成接口文档，详见： http://$IP:$Port/doc.html

## 性能测试
1. 详见《性能测试报告》的Word文档。

