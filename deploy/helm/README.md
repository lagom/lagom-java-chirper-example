# Deploy Chirper with Helm

## Getting Started

Install Helm and deploy it to your cluster

```bash
$ helm init
$HELM_HOME has been configured at /home/username/.helm.

Tiller (the Helm server-side component) has been installed into your Kubernetes Cluster.
Happy Helming!

$ helm version
Client: &version.Version{SemVer:"v2.6.1", GitCommit:"bbc1f71dc03afc5f00c6ac84b9308f8ecb4f39ac", GitTreeState:"clean"}
Server: &version.Version{SemVer:"v2.6.1", GitCommit:"bbc1f71dc03afc5f00c6ac84b9308f8ecb4f39ac", GitTreeState:"clean"}
```

Add the incubator charts and download dependencies (cassandra):

```bash
$ cd deploy/helm
$ helm repo add incubator https://kubernetes-charts-incubator.storage.googleapis.com
$ helm dependency update
```

## Deploy 

### MiniKube

#### Install Minikube

Install `minikube` and `kubeadmn` on your system using the instructions found
in the [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) install
documentation.

> Note: if you already have minikube installed you may want to ensure its clean by
running `$ minikube delete` before running `$ minikube start` below.

Once installed you can start a local kubernetes instance by running:

```bash
$ minikube start --memory 4096
$ kubectl get nodes
NAME       STATUS    AGE       VERSION
minikube   Ready     5m       v1.6.4
```
#### Deploy Chirper to Minikube

```
$ helm install -n chirper .
NAME:   chirper
LAST DEPLOYED: Mon Sep 25 12:07:25 2017
NAMESPACE: default
STATUS: DEPLOYED

RESOURCES:
==> v1/Service
NAME                           CLUSTER-IP  EXTERNAL-IP  PORT(S)                                       AGE
chirper-cassandra              None        <none>       7000/TCP,7001/TCP,7199/TCP,9042/TCP,9160/TCP  1s
activityservice-akka-remoting  10.0.0.196  <none>       2551/TCP                                      1s
activityservice                None        <none>       9000/TCP                                      1s
chirpservice-akka-remoting     10.0.0.7    <none>       2551/TCP                                      1s
chirpservice                   None        <none>       9000/TCP                                      1s
friendservice-akka-remoting    10.0.0.240  <none>       2551/TCP                                      1s
friendservice                  None        <none>       9000/TCP                                      1s
web                            10.0.0.212  <none>       9000/TCP                                      1s
nginx-ingress                  10.0.0.96   <none>       80/TCP                                        1s
nginx-default-backend          10.0.0.57   <none>       80/TCP                                        1s

==> v1beta1/Deployment
NAME                      DESIRED  CURRENT  UP-TO-DATE  AVAILABLE  AGE
nginx-default-backend     1        1        1           0          1s
nginx-ingress-controller  1        1        1           0          1s

==> v1beta1/StatefulSet
NAME               DESIRED  CURRENT  AGE
chirper-cassandra  3        1        1s
activityservice    1        1        1s
chirpservice       1        1        1s
friendservice      1        1        1s
web                1        1        1s

==> v1beta1/Ingress
NAME             HOSTS  ADDRESS  PORTS  AGE
chirper-ingress  *      80       1s
```

Once deployed you should be able to access Chirper via its web UI, minikube can provide those details for
you like so:

```
$ minikube service --url nginx-ingress | head -n 1
http://192.168.99.100:32327
```

Copy and paste the URL provided by the above command into your browser and use the Chirper app.
