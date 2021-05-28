#############################################################
hints for setup
############################################################

build and create docker image:

mvn clean install
pushed auch das image ins docker repo, weiter mit 6.

To compile, build, and push the image to a remote repo:
mvn clean deploy -Ddocker.user=<username> -Ddocker.password=<passwd> -Ddocker.url=<docker-registry-url>

?mvn clean deploy -Ddocker.user=admin -Ddocker.password=admin123 -Ddocker.url=http://10.211.55.4:32000/repository/docker-repo/
mvn clean deploy -Ddocker.user=admin -Ddocker.password=admin123 -Ddocker.url=http://10.211.55.4:32132

curl -v -u admin:admin123 --upload-file /Users/wewer/workspace/yuuvisvapi/target/yuuvis-v-api-1.2-SNAPSHOT.jar http://10.211.55.4:32000/repository/maven-snapshots/juergenwewer/optimal/yuuvis-v-api/1.2-SNAPSHOT/yuuvis-v-api-1.2-SNAPSHOT.jar
curl -v -u admin:admin123 --upload-file /home/jwewer/workspace/yuuvisvapi/target/yuuvis-v-api-1.3-SNAPSHOT.jar http://10.0.1.51:32000/repository/maven-snapshots/juergenwewer/optimal/yuuvis-v-api/1.3-SNAPSHOT/yuuvis-v-api-1.3-SNAPSHOT.jar


docker login http://10.211.55.4:32132
3. docker image ls

4. docker image tag e2faa1e00eaa 10.211.55.4:32132/yuuvis-v-api:1.1-SNAPSHOT
5. docker push 10.211.55.4:32132/yuuvis-v-api:1.1-SNAPSHOT

on the master node:

ssh root@10.211.55.4

(only mac:)
in: ~/.docker/daemon.json

debian:
/etc/docker/daemon.json

add:
{"insecure-registries":["10.211.55.4:32132"]}
on mac, desktop docker restart
on debian:
systemctl daemon-reload
systemctl restart docker
check with docker info:

to test
docker pull 10.211.55.4:32132/yuuvis-v-api:1.6-SNAPSHOT

create helm chart:

helm create nexus
die datei: jwt-secrets.yaml nach nexus/template kopieren

testlauf:
helm template --debug nexus

dann secret generieren:
helm install nexus --generate-name

6. edit the version in api.yaml
7. helm install api --generate-name
funtioniert nicht:
ansible-playbook -i macpro dockersecret.yaml  -v


total:

to build a new version:
edit version in pom
mvn clean package
edit the Dockerfile - put in the version
mvn clean install
edit the version in api helm - deployment.yaml in the image:
helm install api --generate-name
check the service yuuvis-api for the IP -> 30127
http://10.211.55.4:30127/ in the Browser

############################################################################
deploy a new version of yuuvis-api to nexus repository:
############################################################################

Check the ip adress in:
pom.xml

edit version in pom
(sudo) mvn clean install
edit the Dockerfile - put in the version
(sudo) mvn clean deploy


###########################################################################
if nexus was setup a new docker secret has to be generated:
###########################################################################
export KUBECONFIG=/Users/wewer/.kube/master/etc/kubernetes/admin.conf

ssh root@10.211.55.4

docker login http://10.211.55.4:32132
sudo docker login http://10.0.1.51:32132

cat ~/.docker/config.json

sollte einen Eintrag haben, sonst per nano editieren:

{
    "auths": {
    "10.211.55.4:32132": {
    "auth": "YWRtaW46YWRtaW4xMjM="
    }
  }
}
neu:
{
"auths": {
"jw-cloud.org:18443": {}
},
"credsStore": "osxkeychain"
}

kubectl create secret generic regcred \
--from-file=.dockerconfigjson=/root/.docker/config.json \
--type=kubernetes.io/dockerconfigjson --dry-run=client  --output=yaml > optimal-secrets.yaml

neu:
kubectl create secret generic jwcloudcred \
--from-file=.dockerconfigjson=/Users/wewer/.docker/config.json \
--type=kubernetes.io/dockerconfigjson --dry-run=client  --output=yaml > optimal-secrets.yaml

dann:

cat optimal-secrets.yaml

und den inhalt nach
nexus/templates/optimal-secrets.yaml
kopieren ! die secrets sehen ziemlich gleich aus.

unter den metadaten die folgende Zeile ergänzen:
namespace: yuuvis

dann create docker secret:
helm install nexus nexus

###########################################################################
deploy yuuvis-api to the cluster
###########################################################################


create docker secret:
helm install nexus nexus

Check the ip adress in:
api/templates/deployment.yaml


deploy the application:

edit the version in api helm - deployment.yaml in the image:
helm install yuuvis-v-api api

##############################################################################
helm uninstall yuuvis-v-api
helm uninstall nexus


