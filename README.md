# 本项目整合了OCR文字识别与QQ机器人得联动<br/>

部署：
---

- 使用本项目请安装Java8<br/>
- 下载[Go-CqHttp](https://github.com/Mrs4s/go-cqhttp)
- 配置 config.yml 文件中
- 设置为反向代理模式
- 设置 ws-reverse 下得 universal 为 ws://localhost:8080/ws/shiro

- 如以下设置：
- ````
  servers:
    - ws-reverse:
      universal: ws://localhost:8080/ws/shiro
      api: ws://your_websocket_api.server
      event: ws://your_websocket_event.server
      reconnect-interval: 3000
      middlewares:
      <<: *default # 引用默认中间件 
- models文件夹必须与OCR.jar处于同一个目录下

使用到得库：
---
以下是使用得库源地址：<br/>
[OCR文字识别库](https://github.com/mymagicpower/AIAS)<br/>
[Shiro OneBot开发框架](https://github.com/MisakaTAT/Shiro)<br/>
[Spring Boot](https://spring.io/projects/spring-boot/)