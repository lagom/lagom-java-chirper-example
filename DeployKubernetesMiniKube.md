# Minikube setup

Start `minikube`.

```
minikube start
```

Setup docker env.

```
eval $(minikube docker-env)
```

# Deploy Cassandra

Based on https://github.com/kubernetes/kubernetes/tree/master/examples/storage/cassandra

## Setup

Pull the Cassandra docker image.

```
docker pull gcr.io/google-samples/cassandra:v12
```

## Declare Cassandra endpoints

Create Service to expose Cassandra endpoints.

```
kubectl create -f deploy/k8/minikube/cassandra/cassandra-service.yaml
```

Observe the created Service.

```
kubectl get svc cassandra
```

## Create Cassandra ring

Since we're using minikube:

* Only 1 instance.
* No persistent volume.

```
kubectl create -f deploy/k8/minikube/cassandra/cassandra-statefulset.yaml
```

Observe the created StatefulSet.

```
kubectl get statefulset cassandra
```

## Check Cassandra running

Cassandra should be running now.

```
kubectl get pods -l="app=cassandra"
```

Example output:

```
NAME          READY     STATUS    RESTARTS   AGE
cassandra-0   1/1       Running   0          1m
```

Run Cassandra `nodetool`.

```
kubectl exec cassandra-0 -- nodetool status
```

Example output:

```
Datacenter: DC1-K8Demo
======================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
UN  172.17.0.5  99.45 KiB  32           100.0%            446361b8-d005-4525-8830-04d23a43d6aa  Rack1-K8Demo
```

# Publish Chirper

Publish Chirper docker images to `minikube`'s Docker registry.

```
mvn clean package docker:build
```

Once done, check if images has been published.

```
docker images
```

Expected output should be similar to the following.

```
REPOSITORY                                             TAG                 IMAGE ID            CREATED             SIZE
chirper/front-end                                      1.0-SNAPSHOT        1a8ff0f4ba3c        18 minutes ago      145 MB
chirper/front-end                                      latest              1a8ff0f4ba3c        18 minutes ago      145 MB
chirper/load-test-impl                                 1.0-SNAPSHOT        b01800ca5d47        18 minutes ago      150 MB
chirper/load-test-impl                                 latest              b01800ca5d47        18 minutes ago      150 MB
chirper/activity-stream-impl                           1.0-SNAPSHOT        92e1f3060e8b        18 minutes ago      150 MB
chirper/activity-stream-impl                           latest              92e1f3060e8b        18 minutes ago      150 MB
chirper/chirp-impl                                     1.0-SNAPSHOT        a15defc9e551        18 minutes ago      150 MB
chirper/chirp-impl                                     latest              a15defc9e551        18 minutes ago      150 MB
chirper/friend-impl                                    1.0-SNAPSHOT        cee7f72a23ad        19 minutes ago      150 MB
chirper/friend-impl                                    latest              cee7f72a23ad        19 minutes ago      150 MB
```

# Deploy Friend Service

Deploy the Friend Service.

```
kubectl create -f deploy/k8/minikube/lagom/friend-impl/friend-impl-statefulset.json
```

Check deploy status.

```
kubectl get pods -l="app=friendservice"
```

View the complete pod status.

```
kubectl describe pod friendservice
```
