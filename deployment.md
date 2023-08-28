환경 : Docker + Nginx + Spring boot

# Http

1. linux nginx 설치
    
    ```bash
    apt-get install nginx
    ```
    
2. nginx 설정 파일 default.conf 생성
    
    ```bash
    upstream app {
      server eruna:8080; # WAS 컨테이너의 이름
    }
    
    server {
        listen       80;
    
        location / {
            proxy_pass http://app;
        }
    
    }
    ```
    
3. nginx 이미지 빌드하기 위한 Dockerfile 생성
    
    설정한 default.conf 파일을 nginx 컨테이너 내에서 사용하도록 COPY
    
    docker start 후 실행할 명령 CMD 란에 입력
    
    ```bash
    FROM nginx:latest
    
    # 현재 경로에 있는 default.conf를 컨테이너 안 etc/nginx/conf.d/nginx.conf로 복사한다.
    COPY default.conf /etc/nginx/conf.d/nginx.conf 
    
    CMD ["nginx", "-g", "daemon off;"]
    ```
    
4. nginx 도커 이미지 빌드 
    
    Dockerfile 있는 경로에서
    
    ```bash
    docker build -t nginx__1 .
    ```
    

1. jar 앱 Dockerfile 생성 및 빌드
    
    ```bash
    FROM openjdk:17-jdk-alpine
    ARG JAR_FILE=eruna-0.0.1-SNAPSHOT.jar
    COPY ${JAR_FILE} app.jar
    EXPOSE 8080
    ENTRYPOINT ["java", "-jar","/app.jar", "—spring.profiles.active=prod"]
    ```
    
    ```bash
    docker build -t eruna .
    ```
    
2. 두 도커 컨테이너를 한번에 띄우고, 관련 명령어를 다시 입력하지 않기 위한 docker-compose.yml 생성
    
    ```bash
    version: '3'
    services:
      eruna:
          container_name: eruna
          image: "eruna"
          ports:
           - "8080:8080"
      nginx:
          container_name: nginx__1
          image: "nginx__1
          ports:
           - "80:80"
          depends_on:
           - eruna
    ```
    
    컨테이너 이름, 이미지, 포트 포워딩 정보 입력
    
3. docker 컨테이너 띄우기
    
    ```bash
    docker-compose up 
    # docker-compose up -d 를 하게되면 백그라운드로 실행
    ```
    
4. 서버 ip 접속

# Domain 연결

1. nginx 설정파일 default.conf 수정
    
    ```bash
    upstream app {
      server eruna:8080; # WAS 컨테이너의 이름
    }
    
    server {
        listen       80;
        server_name eruna.site;
    
        location / {
            proxy_pass http://app;
        }
    
    }
    ```
    

1. nginx 이미지 빌드
    
    ```bash
    docker build -t nginx__1 .
    ```
    

1. docker-compose 실행(경로 확인)
    
    ```bash
    docker-compose up -d
    ```
    
2. eruna.site 접속

# Https

1. letsencrypt 이용한 인증키 발급
    - certbot 설치
        
        ```bash
        sudo apt install certbot python3-certbot-nginx
        ```
        
    - DNS 를 이용하는 Manual 방식으로 인증하기
        
        → certbot을 실행하고 화면에 표시되는 문구를 DNS 의 TXT에 기록하고, 그것을 Lets 에서 검증하여 인증서를 발급해주는 방식
        
    
    ```bash
    certbot certonly --manual --preferred-challenges dns -d eruna.site
    ```
    
    ![image](https://github.com/Eruna-Jeoruna/jeoruna/assets/47443884/2ba6a2cd-a10d-4e11-8b5f-cd3311ac94f5)
    
    이메일 정기구독 할지? → N
    
    ![image](https://github.com/Eruna-Jeoruna/jeoruna/assets/47443884/6dab5fe1-68fb-4211-92b7-cd1f1ff2d008)
    
    IP 를 저장하는지? → Y 
    
    ![image](https://github.com/Eruna-Jeoruna/jeoruna/assets/47443884/3e7408ce-c985-4e32-acdc-8ee3a9f2bcf7)
    
    등록한 도메인의 갯수만큼 물어본다. 위의 문구를 DNS 에 TXT에 입력
    
    ![image](https://github.com/Eruna-Jeoruna/jeoruna/assets/47443884/f5c2ea97-0918-4aba-ae11-12f2c9f95772)
    
    가비아 dns 관리, 레코드 수정에서 다음과 같이 저장할 수 있음
    
    - 성공 화면
    
    ![image](https://github.com/Eruna-Jeoruna/jeoruna/assets/47443884/89ad9ce5-3a21-48a3-ada2-0a6a4dc81ac8)
    
    - /etc/letsencrypt/live/eruna.site 경로에 4가지 인증키들이 존재하는 것을 확인할 수 있음

1. nginx default.conf 수정
    
    ```bash
    upstream app {
      server eruna:8080; # WAS 컨테이너의 이름
    }
    
    server {
        listen       80;
        server_name eruna.site;
        server_tokens off; # nginx version 명시 안하도록
    
        #location /.well-known/acme-challenge/ {
        #   root /var/www; # webroot 인증 방식을 위한 root 경로 설정
        #}
    
        location / {
            return 301 https://$host$request_uri;
        }   
    
    }
    
    server {
        listen 443 ssl;
        server_name eruna.site;
        server_tokens off;
    
    		# 발급받은 인증키 연결
        ssl_certificate /etc/letsencrypt/live/eruna.site/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/eruna.site/privkey.pem;
        include /etc/letsencrypt/options-ssl-nginx.conf;
        ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
    
        location / {
    			proxy_pass http://app;
        }
    }
    ```
    

1. Dockerfile 수정
    
    ```bash
    FROM nginx:latest
    
    COPY default.conf /etc/nginx/conf.d/nginx.conf
    COPY letsencrypt /etc/letsencrypt
    
    CMD ["nginx", "-g", "daemon off;"]
    ```
    
    컨테이너 내부에서도 인증키 경로 똑같이 사용하도록 COPY
    
    Dockerfile 위치에 인증키가 있는 letsencrypt 폴더와 nginx 설정파일인 default.conf가 있어야 한다
    
2. nginx 이미지 빌드
    
    ```bash
    docker build -t nginx__1 .
    ```
    
3. docker-compose.yml 수정
    
    ```bash
    version: '3'
    services:
      eruna:
          container_name: eruna
          image: "eruna"
          ports:
           - "8080:8080"
      nginx:
          container_name: nginx__1
          image: "nginx__1"
          restart : unless-stopped     
    			# 자동 ssl key 재발급시 수행 명령
          command: "/bin/sh -c 'while :; do sleep 6h & wait $${!}; nginx -s reload; done & nginx -g \"daemon off;\"'"
          ports:
           - "80:80"
           - "443:443"
          depends_on:
           - eruna
      certbot:
          container_name: certbot__1
          image: certbot/certbot
          restart : unless-stopped    
    			# 자동 ssl key 재발급 명령
          entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"
    ```
    
    https 포트 연결
    
4. docker-compose 실행
    
    ```bash
    docker-compose up -d
    ```
    
5. eruna.site 접속
