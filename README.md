# Elasticsearch+Kibana+filebeat+nginx

#### ■Elasticsearch

```shell
# イメージをダウンロード
$ docker pull docker.elastic.co/elasticsearch/elasticsearch:7.10.1

# 起動
$ docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```



#### ■kibana

```shell
# イメージをダウンロード
$ docker pull docker.elastic.co/kibana/kibana:7.10.1

# 起動
$ docker run -d --name kibana --link elasticsearch -p 5601:5601 docker.elastic.co/kibana/kibana:7.10.1
```



#### ■filebeat

```shell
# イメージをダウンロード
$ docker pull docker.elastic.co/beats/filebeat:7.10.1

# 起動
$ docker run \
docker.elastic.co/beats/filebeat:7.10.1 \
setup -E setup.kibana.host=kibana:5601 \
-E output.elasticsearch.hosts=["elasticsearch:9200"]

# filebeat.docker.ymlをダウンロード
$ curl -L -O https://raw.githubusercontent.com/elastic/beats/7.10/deploy/docker/filebeat.docker.yml

# filebeatを設定
$ docker run -d \
  --name=filebeat \
  --link elasticsearch\
  --link kibana\
  --user=root \
  --volume="$(pwd)/filebeat.docker.yml:/usr/share/filebeat/filebeat.yml:ro" \
  --volume="/var/lib/docker/containers:/var/lib/docker/containers:ro" \
  --volume="/var/run/docker.sock:/var/run/docker.sock:ro" \
  docker.elastic.co/beats/filebeat:7.10.1 filebeat -e -strict.perms=false \
  -E output.elasticsearch.hosts=["elasticsearch:9200"]

# ngixを起動
$ docker run \
  --label co.elastic.logs/module=nginx\
  --label co.elastic.logs/fileset.stdout=access \
  --label co.elastic.logs/fileset.stderr=error \
  --label co.elastic.metrics/module=nginx \
  --label co.elastic.metrics/metricsets=status \
  --label co.elastic.metrics/hosts='${data.host}:${data.port}' \
  --detach=true \
  --name my-nginx-app \
  -p 8080:80 \
  nginx:latest

# コンテナを確認
$ docker ps
CONTAINER ID   IMAGE                                                  COMMAND                  CREATED          STATUS          PORTS                                            NAMES
b6068f335e24   nginx:latest                                           "/docker-entrypoint.…"   10 minutes ago   Up 10 minutes   0.0.0.0:8080->80/tcp                             my-nginx-app
0506629f8870   docker.elastic.co/beats/filebeat:7.10.1                "/usr/local/bin/dock…"   13 minutes ago   Up 12 minutes                                                    filebeat
b2f2b0ed7519   docker.elastic.co/kibana/kibana:7.10.1                 "/usr/local/bin/dumb…"   19 minutes ago   Up 19 minutes   0.0.0.0:5601->5601/tcp                           kibana
712e11435d05   docker.elastic.co/elasticsearch/elasticsearch:7.10.1   "/tini -- /usr/local…"   23 minutes ago   Up 23 minutes   0.0.0.0:9200->9200/tcp, 0.0.0.0:9300->9300/tcp   elasticsearch

```



#### ■nginx

```shell
# イメージをダウンロード
$ docker pull nginx

# 起動
$ docker run \
  --label co.elastic.logs/module=nginx\
  --label co.elastic.logs/fileset.stdout=access \
  --label co.elastic.logs/fileset.stderr=error \
  --label co.elastic.metrics/module=nginx \
  --label co.elastic.metrics/metricsets=status \
  --label co.elastic.metrics/hosts='${data.host}:${data.port}' \
  --detach=true \
  --name my-nginx \
  -p 80:80 \
  nginx:latest
```



#### ■kibanaを確認

![image-20210112233601016](C:\Users\ysy\AppData\Roaming\Typora\typora-user-images\image-20210112233601016.png)
