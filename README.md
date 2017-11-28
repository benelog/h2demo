## 발표자료
- https://github.com/benelog/h2demo/blob/master/docs/README.md



## DEMO 1 : Nginx + Spring Boot

### (사전 준비) Nginx 설정
Ubuntu 16.0.4 기준으로 nginx 설치는 아래 자료 참조한다. 검증하는 예제를 그대로 실행하려면 도메인은 `h2demo.net` 으로 설정한다.

- https://www.digitalocean.com/community/tutorials/how-to-set-up-nginx-with-http-2-support-on-ubuntu-16-04

위의 안내대로 설치했다면 `/etc/nginx/sites-available/default` 파일에 아래와 같이 Tomcat과의 연결을 설정한다. 
```
        server_name h2demo.net;

        location / {
		oxy_set_header  Host $host;
		proxy_set_header  X-Real-IP $remote_addr;
		proxy_set_header  X-Forwarded-For $remote_addr;
		proxy_set_header  X-Forwarded-Host h2demo.net;
		proxy_pass http://localhost:8080;
       }


```

HTTP2에 대한 선언은 `listen 443` 구절이 있는 지점에서 추가한다.

```
        listen 443 ssl http2 default_server;
        listen [::]:443 ssl http2 default_server;

```

설치가 되었다면 아래와 같이 시작할 수 있다.
```
sudo service nginx start
```

/etc/hosts 파일에 `127.0.0.1 h2demo.net`을 추가한다.


### Spring Boot 1.5.7 + Tomcat 8.5
- [pom.xml](https://github.com/benelog/h2demo/blob/h2-boot-1.5.7/pom.xml) : 의존성 선언
- [Http2DemoApplication.java](https://github.com/benelog/h2demo/blob/h2-boot-1.5.7/src/main/java/net/h2demo/Http2DemoApplication.java) : h2 프로토콜 업그레이드 선언
- [HomeController.java](https://github.com/benelog/h2demo/blob/h2-boot-1.5.7/src/main/java/net/h2demo/HomeController.java) : `/` 주소로 요청이 오면 index.html으로 연결
- [index.html](https://github.com/benelog/h2demo/blob/master/src/main/resources/templates/index.html) : 보여줄 페이지

Tomcat 실행은 아래와 같이 할 수 있다.

```bash
git clone https://github.com/benelog/h2demo.git
cd h2demo
git checkout h2-boot-1.5.7
./mvnw spring-boot:run
```

혹은 IDE에서 [Http2DemoApplication.java](https://github.com/benelog/h2demo/blob/h2-boot-1.5.7/src/main/java/net/h2demo/Http2DemoApplication.java) 을 실행한다.

### 검증
웹브라우져와 아래 예제로 h2, h2c 연결이 제대로 되었는지 검증할 수 있다.

- RestTemplate 예제
	- [H2ClientTest.java](https://github.com/benelog/h2demo/blob/master/src/test/java/net/h2demo/H2ClientTest.java) : RestTemplate + (OkHttp, Netty, JDK HttpURLConnection) 
- JShell + Java9 HttpClient
	- [h2demo.jsh](https://github.com/benelog/h2demo/blob/master/h2demo.jsh) : 8080 포트로 h2c연결이 되는지 확인할수 있는 테스트 스크립트
	- [dev-h2demo.jsh](https://github.com/benelog/h2demo/blob/master/dev-h2demo.jsh) : 443 포트로 인증서를 검증하지 않고 연결하는 테스트 스크립트
-  [capture-8080.sh](https://github.com/benelog/h2demo/blob/master/capture-8080.sh) : tshark로 8080 포트 모니터링
)

JShell로 작성한 `dev-h2demo.jsh` 와 같은 파일들은 Linux 명령행에서 `./dev-h2demo.jsh`와 같이 바로 실행할 수 있다.

### Spring Boot 2.0.M6 + Tomcat 9, Server push
- [commit 8850](https://github.com/benelog/h2demo/commit/885081c9445f8bc10e04390bb53c09e84785baec) : 버전을 올리고 server push를 추가하는 commit
- [pom.xml](https://github.com/benelog/h2demo/blob/master/pom.xml)
- [Http2DemoApplication.java](https://github.com/benelog/h2demo/blob/master/src/main/java/net/h2demo/Http2DemoApplication.java) : Spring Boot2에 맞도록 h2 프로토콜 업그레이드 선언
- [HomeController.java](https://github.com/benelog/h2demo/blob/master/src/main/java/net/h2demo/HomeController.java) : Server push 추가


아래와 같이 master branch를 바꿔서 Tomcat을 올리면 해당 버전의 예제가 실행된다.
```
git checkout master
./mvnw spring-boot:run
```

`https://h2demo.net/faster`로 접속해서 Server push가 제대로 동작하는지 확인한다.

## DEMO 2: HAProxy + Spring Boot
### (사전준비) HAProxy 설정
HAProxy설치는 아래 링크를 참조한다. Jetty에 대한 부분은 이번 Demo와는 상관 없으므로 신경을 쓰지 않아도 된다.

- https://www.eclipse.org/jetty/documentation/current/http2-configuring-haproxy.html

### HAProxy 실행
Demo1에서 이어서 한다면 Nginx를 내리고 HAProxy 시작한다.

```
sudo service nginx stop
sudo service haproxy start
```


## 환경 구성팁
### tshark를 root 권한으로 실행하지 않기
tshark를 root 권한으로 실행하면 보안상 좋지 않다. 아래와 같은 스크립트로 tshark를 실행할 수 있는 별도의 사용자 그룹을 만들고, setcap으로 필요한 기능만을 부여한다.

```
sudo groupadd tshark
sudo usermod -a -G tshark benelog
sudo chgrp tshark /usr/bin/dumpcap
sudo chmod 750 /usr/bin/dumpcap
sudo setcap cap_net_raw,cap_net_admin=eip /usr/bin/dumpcap
getcap /usr/bin/dumpcap
```


### CentOS 6에서 HTTP/2를 위한 Nginx 설치
ALPN지원을 위해서넌 openssl 1.0.2버전 이상이 필요하다. 그러나 CentOS 6에서는 yum update로 openssl버전을 해당버전까지 현재 올릴 수가 없다.

따라서 CentOS 6에서는 1.0.2 버전이상의 openssl을 따로 다운로드해서 Nginx를 빌드할 때 사용한다.

```
# openssl 소스 다운로드
wget https://www.openssl.org/source/openssl-1.0.2m.tar.gz
tar xvfz openssl-1.0.2m.tar.gz

# nginx를 빌드할 디렉토리로 이동해서 실행
# --with-openssl 뒤에는 openssl 소스를 다운받은 경로를 지정
./configure --with-http_ssl_module --with-http_v2_module  --with-openssl=/home/benelog/download/openssl-1.0.2m  --prefix=/home/benelog/apps/nginx
./make
./make install

```



