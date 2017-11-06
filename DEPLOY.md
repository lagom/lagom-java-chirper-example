# Deploying Chirper

This page describes the steps required to deploy the Lagom Chirper example using the [Enterprise Suite Orchestration and its Developer Sandbox](https://www.lightbend.com/product/conductr/developer). The orchestration component is known as ConductR.

The Sandbox provides an environment very close to production so that you can conveniently run your services on your local machine. The same ConductR images used in production are used by the Developer Sandbox. It allows developers to deploy multiple services, and to run these services exactly how they will be run in production with clustering, service location, dynamic proxying and various other features.

This page is written as a companion to the book [Deploying Reactive Microservices: Strategies for Delivering Resilient Systems](http://todo). However, if you arrive to this page without having read the book then you should still be able to follow the steps outlined by this page.

We will now go through some of the features provided by ConductR to help you manage and scale your distributed systems.

## Register with Lightbend.com

Please consider registering with [Lightbend.com](https://www.lightbend.com/account/register). Registration is free and it will allow you to obtain a copy of the Lightbend Enterprise Suite suitable for deployment into production.

For a microservice based system, it is expected to have multiple instances of various services to be running at the same time. The free version of the Enterprise Suite allows _each_ of these services to be scaled up to three instances. As part of the feature tour below, we will be scaling the `Activity Stream` service within the Lagom Chirper example to 3 instances each.

Once you have registered, be sure to download the ebook [Deploying Reactive Microservices: Strategies for Delivering Resilient Systems](https://todo) available free for registered users. This ebook is a follow on from [Reactive Microservices Architecture](https://todo) and [Developing Reactive Microservices](https://todo), which will also be freely available to download once registered.

## Prerequisites

Please ensure the following prerequisites are present before starting with the actual deployment.

#### Operating Systems

If you are using Linux or MacOS, please continue to the next step.

If you are using Windows, please install a Linux VM, preferably `Ubuntu 16.04 LTS`. The Linux VM is required for running Developer Sandbox on Windows.

If you do not wish to install a Linux VM at this stage, you will still be able to deploy the Lagom Chirper example on top of ConductR running on [AWS](#Deploying_to_AWS).

#### JDK 8 or JRE 8

Please ensure you have either JDK 8 or JRE 8 installed from either OpenJDK or Oracle.

_If you are using Windows, please ensure either JDK 8 or JRE 8 installed from either OpenJDK or Oracle is installed on the Linux VM._

#### Git

Please ensure you have [`git`](https://git-scm.com/downloads) installed since we will be cloning the Lagom Chirper example.

_If you are using Windows, please ensure `git` is installed on the Linux VM._

#### Build tools

The Lagom Chirper example supports the use of both Maven and SBT for building. Please choose the build tool familiar to you.

##### Maven

[Maven](https://maven.apache.org/) is one build tool that Lagom Chirper is configured for. If you wish to try out the Maven support, make sure that you've [installed it](https://maven.apache.org/download.cgi). You'll also need to install [Docker](https://www.docker.com/).

If you are using Windows, please ensure `mvn` and `docker` are installed on the Linux VM.

##### SBT

[SBT](http://www.scala-sbt.org/download.html) is another build tool used by Lagom Chirper.

If you haven't installed [SBT](http://www.scala-sbt.org/download.html) and wish to use the SBT build of Chirper, proceed with either installation steps for [Linux](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html) or [MacOS](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Mac.html).


_If you are using Windows, please ensure `sbt` is installed on the Linux VM._

### Docker

The ConductR and Developer Sandbox don't have any direct dependencies on Docker. As such, it's possible to run them without Docker being installed. However, Lagom Chirper works better with the Dynamic Proxying feature provided by the Enterprise Suite. Since the Developer Sandbox provides this feature via a Docker container, deploying Lagom Chirper to the sandbox will require the installation of [Docker](https://www.docker.com/). Additionally, if you're using the Maven build of Chirper you'll also need to install Docker.

_If you are using Windows and wish to continue with Docker installation, please ensure Docker is installed on the Linux VM._

## Deploying Lagom Chirper

This section describes the steps to deploy Lagom Chirper example into the Developer Sandbox.

### Enterprise Suite Sandbox setup

Follow the Enterprise Suite Sandbox [installation guide](http://www.lightbend.com/product/conductr/developer). The whole process should take less than 10 minutes.

To save time here, you may choose stop at the end of **Inspect the Sandbox** step, and not continue with **Loading Reactive Maps** step and beyond.

### Clone the Lagom Chirper example

Clone the Lagom Chirper example into your machine.

```bash
mkdir -p ~/examples
cd examples
git clone https://github.com/lagom/lagom-java-chirper-example.git
```

### Deploying Lagom Chirper

Start the sandbox.

```bash
sandbox run 2.0.0 -n 3 -f visualization
```

Go to the cloned Lagom Chirper directory

```bash
cd ~/examples/lagom-java-chirper-example
```

Deploy the Lagom Chirper application. It is packaged as both SBT and Maven. You'll want to pick the build tool you're most comfortable with.

Maven:
```bash
./mvn-install
```

SBT:
```bash
sbt install
```

When you deploy Chirper for the first time, it may take a few minutes since the build tool will need to download all the artefacts required by the project. On subsequent deploys, the time it takes for the tasks to complete will be significantly faster.

Once the artefacts are downloaded, the following tasks will be performed:

* Build the local configuration for Cassandra.
* Build the Bundle for `Activity Stream`, `Chirp`, `Friend`, and `Front-End` service respectively.
* Deploy and run Cassandra using the built local configuration.
* Deploy and run `Activity Stream`, `Chirp`, `Friend`, and `Front-End` Bundles respectively.

_Bundle_ is a special term coined by ConductR:

> A bundle is an archive of components along with meta data (a bundle descriptor file) describing how the files should be executed. Bundles are similar to executable JARs with the difference being that they are not constrained to executing just JVM code. Bundles are also named using a digest of their contents so that their integrity may be assured. Bundles represent a unit of software that may have a release cycle which is distinct from other bundles.

Once deployed, the `conduct info` command will allow you to inspect the state of the deployed services. When you run `conduct info`, you should see something similar to this.

```
Felixs-MBP-2:lagom-java-chirper-example felixsatyaputra$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
89fe6ec          activity-stream-impl   v1     1     0     1  web
73595ec          visualizer             v2     1     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     1     0     1  haproxy
6ac8c39          load-test-impl         v1     1     0     1  web
9a2acf1-44e4d55  front-end              v1     1     0     1  web
3349b6b          eslite                 v1     1     0     1  elasticsearch
01dd0af          friend-impl            v1     1     0     1  web
d842342          chirp-impl             v1     1     0     1  web
1acac1d          cassandra              v3     1     0     1  cassandra
```

The frontend of the Chirper application will be available as well. Open http://192.168.10.1:9000/ in your browser to visit the Chirper frontend.

Congratulations - you have now deployed Lagom Chirper into the Developer Sandbox!

### Lagom Chirper quick tour

Lagom Chirper is an example application which showcases Lagom features with functionality similar to Twitter. Since the functionality of Twitter is widely understood, we feel this is provides a readily familiar example.

In Lagom Chirper's vocabulary a `chirp` is equivalent to Twitter's `tweet`.

Lagom Chirper comprises of the following services:
- `Chirp` service: responsible for the storage of `chirps`, and to provide interface to obtain the stored `chirps`.
- `Friend` service: responsible for the storage of `users` and managing the relationship between the stored `user` their `friends`.
- `Activity Stream` service: provides a stream of `chirps` given a particular user. Dependent on the `Chirp` service and `Friend` service.
- `Front-End` service: provides the web based user interface.

The `Activity Stream`, `Chirp`, and `Friend` services are written in Lagom, while `Front-End` service is written in [Playframework](https://www.playframework.com/).


### Enterprise Suite feature tour

#### Service orchestration

When an application is deployed, ConductR will ensure every instance is started one after another, in an orderly manner. Once an instance has been started successfully and confirmed to be healthy, only then the attempt to start the next instance proceeds.

An application signals ConductR that it's ready to start by hitting a REST endpoint, or alternatively by invoking a check command that will call the same REST endpoint underneath. If your application is written in [Lagom](https://www.lagomframework.com/) or [Play](https://www.playframework.com/), the health check will be invoked automatically by the inclusion of [conductr-lib](https://github.com/typesafehub/conductr-lib). For non-Lagom and non-Play application, the [Signalling application state](http://conductr.lightbend.com/docs/2.0.x/SignalingApplicationState) page in the documentation has further details.

If an application fails to start, ConductR will attempt to restart the application for a given number of configurable attempts.

The predictable application start behavior allows scripting of the deployment using simple bash script and the [CLI](http://conductr.lightbend.com/docs/2.0.x/CLI). We believe this simplicity is an advantage as there is no need for operations to learn new language or DSL to start creating their own deployment script.

To see an example of a deployment script from Lagom Chirper, execute the following command.


Maven:
```bash
cat ./mvn-install
```

SBT:

```bash
sbt generateInstallationScript && cat target/install.sh
```

Please refer to the [Orchestrating deployment using the CLI](http://conductr.lightbend.com/docs/2.0.x/DeployingBundlesOps#Using-CLI-to-orchestrate-bundle-deployments) section in the documentation for further details.

The fact that application is started one at a time in a predictable manner also means that ConductR is able to form a cluster for this application in a consistent and reliable way.

For applications written in [Lagom](https://www.lagomframework.com/) with Akka Clustering enabled, ConductR is able to form the Akka cluster automatically.

For non-Lagom applications, the [Akka Clustering](http://conductr.lightbend.com/docs/2.0.x/AkkaAndPlay#Akka-Clustering) page in the documentation provides the instructions so ConductR is able to form the Akka cluster for your application.

The cluster formation feature also works with a non-Akka based clustering. [ConductR Zookeeper](https://github.com/typesafehub/conductr-zookeeper) bundle is an example of this where ConductR forms the Zookeeper cluster which is a non-Akka based.

The service orchestration and cluster formation feature from ConductR also works with a non-JVM apps. The [Postgrest BDR](https://github.com/huntc/postgres-bdr) bundle is an example of Docker based bundle where ConductR is able to form the Postgres cluster.


#### Elasticity and scalability

It's simple to scale services up and down.

We will be scaling `Activity Stream` service up to `2` instances by executing the following command.

```bash
conduct run activity-stream-impl --scale 2
```

Output similar to the following will be displayed. The CLI tool will wait for `Activity Stream` to be scaled to `2 instances`.

```
$ conduct run activity-stream-impl --scale 2
Bundle run request sent.
Bundle 39f36b39adcd108abdc1c599a446d717 waiting to reach expected scale 2
Bundle 39f36b39adcd108abdc1c599a446d717 has scale 1, expected 2
Bundle 39f36b39adcd108abdc1c599a446d717 expected scale 2 is met
Stop bundle with: conduct stop 39f36b3
Print ConductR info with: conduct info
```

To bring `Activity Stream` service back to `1` instance, execute the following command.

```bash
conduct run activity-stream-impl --scale 1
```

Output similar to the following will be displayed.

```
$ conduct run activity-stream-impl --scale 1
Bundle run request sent.
Bundle 39f36b39adcd108abdc1c599a446d717 waiting to reach expected scale 1
Bundle 39f36b39adcd108abdc1c599a446d717 expected scale 1 is met
Stop bundle with: conduct stop 39f36b3
Print ConductR info with: conduct info
```

When services are scaled up, the declared resource profile will be used to decide where the service will be run. The agent node which has available resource in terms of CPU, memory, and disk space which hasn't had the service running will be selected to start the new instance.

The resource profile is declared as part of [bundle configuration](http://conductr.lightbend.com/docs/2.0.x/BundleConfiguration) - please refer to the `nrOfCpus`, `memory`, and `diskSpace` settings in the documentation page.

If role matching is enabled, only agent nodes that have all the roles declared by the `roles` settings will be eligible to run the service.


#### Process Resilience

ConductR monitors the services that it has started, and will ensure the number of requested instance for each services are always met.

When the service process is unexpectedly terminated, ConductR will restart the process until the number of requested instance is met. In the scenario where there are multiple processes to be started, ConductR will ensure the process will be restarted one after another in an orderly fashion.

In the situation when the service is unexpectedly terminated due to loss of hardware (i.e. hardware failure), ConductR will attempt to start the service in the remaining machines where the service is not running until the number of requested scale is met.

In the hardware failure scenario, it's possible that the number requested instances can't be met due to an insufficient number machines being available. However when a replacement machine is commissioned and joins the cluster, ConductR will automatically attempt to start the interrupted service until the number of requested instances are met.

Should the service is deployed with a bundle configuration, the bundle configuration will be automatically applied to the new instance started to replace the one that was unexpectedly terminated.

This resilient behavior relieves operations from the burden of having to start the services whenever there's an unexpected termination. Should the service interruption is caused by hardware failure, operations can simply focus on hardware recovery and let ConductR handle the recovery of the service instances.

With this feature walk-through, we will be simulating the automatic process recovery by terminating one of the service within Lagom Chirper.

Please scale `Activity Stream` to `2` instances.

```
conduct run activity-stream-impl --scale 2
```

Once scaled, the overall state should be similar to this. The `activity-stream-impl` which is the bundle name of the `Activity Stream` service now has `2` running instances.

```
$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
89fe6ec          activity-stream-impl   v1     1     0     2  web
73595ec          visualizer             v2     1     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     1     0     1  haproxy
6ac8c39          load-test-impl         v1     1     0     1  web
9a2acf1-44e4d55  front-end              v1     1     0     1  web
3349b6b          eslite                 v1     1     0     1  elasticsearch
01dd0af          friend-impl            v1     1     0     1  web
d842342          chirp-impl             v1     1     0     1  web
1acac1d          cassandra              v3     1     0     1  cassandra
```

We should be able to see `2` process ids belonging to `Activity Stream` service. Take note of the value of both process ids.

```bash
$ pgrep -f activity-stream-impl
```

Let's kill one of the processes.

```bash
$ pgrep -f activity-stream-impl | head -n 1 | xargs kill
```

Eventually we should still see `2` process ids belonging to `Activity Stream` service, with a new process id replacing the one we killed. The new process is being started quite quickly, and it's very likely you will see the new process id when you run this command.

```bash
$ pgrep -f activity-stream-impl
```

You may choose to scale `Activity Stream` back down to `1` instance if you wish.

```bash
conduct run activity-stream-impl --scale 1
```

#### Rolling upgrade of the app

Performing rolling upgrades on ConductR is relatively straightforward.

A new version of a service can be deployed and run alongside an existing version. Should the new version expose the same endpoint as the existing, while running alongside each other the traffic from the proxy will be delivered to both new and existing versions in round-robin fashion.

In this feature tour, we will be performing a rolling upgrade of the `Friend` service.

There is an instance of `Friend` service already running - look for the bundle with `friend-impl` as its `NAME`. Note the `ID` of the `friend-impl` service - it has `01dd0af` as its value. We call this identifier "Bundle Id".

```
$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
89fe6ec          activity-stream-impl   v1     1     0     1  web
73595ec          visualizer             v2     1     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     1     0     1  haproxy
6ac8c39          load-test-impl         v1     1     0     1  web
9a2acf1-44e4d55  front-end              v1     1     0     1  web
3349b6b          eslite                 v1     1     0     1  elasticsearch
01dd0af          friend-impl            v1     1     0     1  web
d842342          chirp-impl             v1     1     0     1  web
1acac1d          cassandra              v3     1     0     1  cassandra
```

Firstly, we'll build a newer version of the `Friend` service. There's no change in the binary as such, but the process of the rolling upgrade will be the same regardless.

Maven:
```bash
mvn clean package docker:build && \
docker save chirper/friend-impl | bndl --no-default-check --endpoint friend --endpoint akka-remote | conduct load
```

SBT:
```bash
sbt friend-impl/clean friend-impl/bundle:dist && \
conduct load -q $(find friend-impl/target -iname "friend-impl-*.zip" | head -n 1) | xargs conduct run
```

We now have a new instance of the `Friend` service running alongside the existing one. The existing `Friend` service has `01dd0af` as its Bundle Id while the new one has `87375e6`.

```
$ conduct info
Felixs-MBP-2:lagom-java-chirper-example felixsatyaputra$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
89fe6ec          activity-stream-impl   v1     1     0     1  web
73595ec          visualizer             v2     1     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     1     0     1  haproxy
6ac8c39          load-test-impl         v1     1     0     1  web
9a2acf1-44e4d55  front-end              v1     1     0     1  web
3349b6b          eslite                 v1     1     0     1  elasticsearch
01dd0af          friend-impl            v1     1     0     1  web
87375e6          friend-impl            v1     1     0     1  web
d842342          chirp-impl             v1     1     0     1  web
1acac1d          cassandra              v3     1     0     1  cassandra
```

At this point any HTTP requests made to the `Friend` service through the proxy will be delivered round-robin fashion between `01dd0af` and `87375e6`. The lookup to the `Friend` service through ConductR service locator will also be rotated between `01dd0af` and `87375e6`.


Let's stop and undeploy the existing `Friend` service. Note we are using the Bundle Id `3ff82bf` to refer to the existing `Friend` service as the name `friend-impl` is associated to both `01dd0af` and `87375e6`.

```bash
conduct stop 01dd0af
conduct unload 01dd0af
```

You have now performed a rolling upgrade of the `Friend` service!

#### Dynamic proxying

The dynamic proxying feature provides location transparency of your services to its clients. The caller of your services will only need to know the address of the proxy, and its requests will be routed to the available instance regardless of service address changes, or instances going up or down.

ConductR allows services to expose its endpoints to be accessible via proxy. The proxy configuration will be updated as services being scaled up or down, ensuring that the requests being made to these services via the proxy will be routed to the available instance.


In this feature tour, we will scale the Lagom Chirper `Activity Stream` up and down, and we will observe the automatic changes made to the proxy configuration.

ConductR uses [HAProxy](http://www.haproxy.org) as its proxying solution. In the Developer Sandbox, HAProxy is started as docker image.

To view the HAProxy configuration, execute the following command.

```bash
docker exec -ti sandbox-haproxy cat /usr/local/etc/haproxy/haproxy.cfg
```

We should see the HAProxy `frontend` configuration similar to the following. For those unfamiliar with HAProxy, the HAProxy `frontend` accepts the incoming request, then routes the request to the corresponding HAProxy `backend`. The HAProxy `backend` has one or more addresses which is able to service the actual request, and by default HAProxy will route the request to each address in round-robin fashion.

```
frontend default-http-frontend
  mode http
  bind 0.0.0.0:9000
  acl 109543549cf35b5dd03a3889026fb26a-chirpservice-acl-0 path_beg /api/chirps/live
  use_backend 109543549cf35b5dd03a3889026fb26a-chirpservice-backend-0 if 109543549cf35b5dd03a3889026fb26a-chirpservice-acl-0
  acl 109543549cf35b5dd03a3889026fb26a-chirpservice-acl-1 path_beg /api/chirps/history
  use_backend 109543549cf35b5dd03a3889026fb26a-chirpservice-backend-1 if 109543549cf35b5dd03a3889026fb26a-chirpservice-acl-1
  acl f13f7afeaa61c39b13e0f6f5ee62bb5e-activityservice-acl-0 path_beg /api/activity
  use_backend f13f7afeaa61c39b13e0f6f5ee62bb5e-activityservice-backend-0 if f13f7afeaa61c39b13e0f6f5ee62bb5e-activityservice-acl-0
  acl 29006cf84cefda6cb0766f404683f689-friendservice-acl-0 path_beg /api/users
  use_backend 29006cf84cefda6cb0766f404683f689-friendservice-backend-0 if 29006cf84cefda6cb0766f404683f689-friendservice-acl-0
  acl 64c3101863b04857425005e94bd87b16-44e4d555e89ef60bf376d447489b9b10-web-acl-0 path_beg /
  use_backend 64c3101863b04857425005e94bd87b16-44e4d555e89ef60bf376d447489b9b10-web-backend-0 if 64c3101863b04857425005e94bd87b16-44e4d555e89ef60bf376d447489b9b10-web-acl-0
```

The Lagom Chirper `Activity Stream` exposes an endpoint on the `/api/activity` path. This endpoint is called `activityservice`. In the HAProxy `frontend` configuration shown above, requests matching the acl which has the name ending with `activityservice-acl-0` will be routed to the HAProxy `backend` configuration which has the name ending with `activityservice-backend-0`.

The HAProxy backend configuration which has the name ending with `activityservice-backend-0` should be similar to the following.  

```
backend f13f7afeaa61c39b13e0f6f5ee62bb5e-activityservice-backend-0
  mode http

  server 19216810310452 192.168.10.3:10452 maxconn 1024
```

Since there is only `1` instance of the Lagom Chirper `Activity Stream` running, there's only `1` single address listed.

Execute the following command to scale the Lagom Chirper `Activity Stream` to `2` instances.

```bash
conduct run activity-stream-impl --scale 2
```

View the HAProxy configuration once more by executing the following command.

```bash
docker exec -ti sandbox-haproxy cat /usr/local/etc/haproxy/haproxy.cfg
```

The backend configuration will now have an additional address automatically added. This newly added address points to the newly started Lagom Chirper `Activity Stream` service instance. If you access the `Activity Stream` URL on http://192.168.10.1:9000/api/activity repeatedly, HAProxy will round-robin between these two addresses.

```
backend f13f7afeaa61c39b13e0f6f5ee62bb5e-activityservice-backend-0
  mode http

  server 19216810310452 192.168.10.3:10452 maxconn 1024
  server 19216810210822 192.168.10.2:10822 maxconn 1024
```

Similarly the configuration will be updated Lagom Chirper `Activity Stream` service is scaled down.

When the Lagom Chirper `Activity Stream` service is stopped, the entry from the HAProxy configuration is removed.

ConductR allows for full customization of the HAProxy configuration. Operators familiar with HAProxy may elect to fully customise the HAProxy template, and thus gain the complete control on how proxying should be implemented while having the configuration updated automatically in the events of service changes.

Refer to the [Dynamic Proxy Configuration documentation](http://conductr.lightbend.com/docs/2.0.x/DynamicProxyConfiguration) for more details.

#### Service locator

The [Enterprise Suite](https://www.lightbend.com/platform/production) Service Locator provided by ConductR allows a caller of a particular service to lookup the service address, and thus allowing the request from the caller to be made to the correct address. This pattern is particularly important in the deployment of microservice based application, since services are expected to be scaled up or down, or sometimes moved for various reasons.

As such, the list of address to all services running within the system needs to be maintained and kept up to date.

When using an orchestration product that comes with built-in service registry such as ConductR, one can expect this list will be kept updated automatically.

We will show Service Locator functionality within ConductR by trying to lookup a particular user from the `Friend` service within the Lagom Chirper example.

Firstly, we need to register a user. Visit the `Front-End` address at http://192.168.10.1:9000/, click on the "Sign Up" button to view the registration page. Enter the `username` and `Name` as `joe` and `Joe` respectively, and click the "Submit" button.

The user `joe` can be looked up from the `Friend` service through the proxy URL.

```bash
curl http://192.168.10.1:9000/api/users/joe
```

You should see the following JSON response.

```
{"userId":"joe","name":"Joe","friends":[]}
```

Next we will try to lookup `joe` from the `Friend` service through the service locator. To do that we will need to lookup the `Friend` API from the service locator.

Issue the following command to see the endpoints that can be looked up via the service locator.

```bash
conduct service-names
```

You should see the output similar to the following.

```bash
Felixs-MBP-2:lagom-java-chirper-example felixsatyaputra$ conduct service-names
SERVICE NAME     BUNDLE ID        BUNDLE NAME           STATUS
activityservice  89fe6ec          activity-stream-impl  Running
cas_native       1acac1d          cassandra             Running
chirpservice     d842342          chirp-impl            Running
elastic-search   3349b6b          eslite                Running
friendservice    01dd0af          friend-impl           Running
loadtestservice  6ac8c39          load-test-impl        Running
visualizer       73595ec          visualizer            Running
web              9a2acf1-44e4d55  front-end             Running
```

The `BUNDLE NAME` for the `Friend` service is called `friend-impl`, and it exposes its endpoint called `friendservice`.

ConductR exposes its service locator on port `9008`, and in the Developer Sandbox the service locator is accessible on `http://192.168.10.1:9008`.

Let's find the addresses for `friendservice` by executing the following command.

```bash
curl -v http://192.168.10.1:9008/service-hosts/friendservice
```

You should see a JSON list containing the host address and bind port of the `friendservice` similar to the following.

```
["192.168.10.2:10785"]
```

Let's scale `Friend` service to `2` instances.

```bash
conduct run friend-impl --scale 2
```

You should see the list of `friendservice` host address updated accordingly.

```
$ curl http://192.168.10.1:9008/service-hosts/friendservice
["192.168.10.2:10785","192.168.10.3:10373"]
```

ConductR monitors the service it has started and keeps the list of address of the service up to date. The list is automatically updated whenever there is a change to the number of scale due to request from the operator, or due to unexpected service interruption.

Since the service address list is automatically maintained by ConductR, applications are relieved from the burden of registering and deregistering itself with the service registry.

ConductR service locator also provides a HTTP redirection service to the  `friendservice` endpoint.

Execute the following command to invoke `friendservice` endpoint via HTTP redirection.

```
curl -L http://192.168.10.1:9008/services/friendservice/api/users/joe
```

You should see the following JSON response.

```
{"userId":"joe","name":"Joe","friends":[]}
```

Let's examine the HTTP request much closer.

The URL of the request is `http://192.168.10.1:9008/services/friendservice/api/users/joe` and it is comprised of the following parts:

- `http://192.168.10.1:9008/services` is the base URL of the HTTP redirection service provided by the service locator.
- The next part of the URL is `friendservice` which is the name of the endpoint we would like to be redirected to.
- The remaining parts of the URL `/api/users/joe` will form the actual redirect URL to the `friendservice` endpoint.

We can see this exchange if we were to execute the `curl` command with the verbose switch `-v` enabled.

```bash
curl -v -L http://192.168.10.1:9008/services/friendservice/api/users/joe
```

You should see the following output.

```
$ curl -v -L http://192.168.10.1:9008/services/friendservice/api/users/joe
*   Trying 192.168.10.1...
* Connected to 192.168.10.1 (192.168.10.1) port 9008 (#0)
> GET /services/friendservice/api/users/joe HTTP/1.1
> Host: 192.168.10.1:9008
> User-Agent: curl/7.43.0
> Accept: */*
>
< HTTP/1.1 307 Temporary Redirect
< Location: http://192.168.10.2:10785/api/users/joe
< Cache-Control: private="Location", max-age=60
< Server: akka-http/10.0.0
< Date: Fri, 24 Mar 2017 06:17:37 GMT
< Content-Type: text/plain; charset=UTF-8
< Content-Length: 50
<
* Ignoring the response-body
* Connection #0 to host 192.168.10.1 left intact
* Issue another request to this URL: 'http://192.168.10.2:10785/api/users/joe'
*   Trying 192.168.10.2...
* Connected to 192.168.10.2 (192.168.10.2) port 10785 (#1)
> GET /api/users/joe HTTP/1.1
> Host: 192.168.10.2:10785
> User-Agent: curl/7.43.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Length: 42
< Content-Type: application/json; charset=utf-8
< Date: Fri, 24 Mar 2017 06:17:37 GMT
<
* Connection #1 to host 192.168.10.2 left intact
{"userId":"joe","name":"Joe","friends":[]}
```

Note there are `2` HTTP request/response exchanges on the output above.

<receipt of first request>

The first response is replied with HTTP status code `307`, which is a redirect to the address where one of the `friendservice` endpoint resides. The redirect location is declared by the `Location` response header at `http://192.168.10.2:10785/api/users/joe`

The `curl` command is set to automatically follow redirect by supplying the `-L` flag. As such, the second HTTP request is then automatically made to `http://192.168.10.2:10785/api/users/joe`.

The service locator HTTP redirection feature allows performing service lookup with minimal change to the caller's code. The caller code does not need to bear the burden of performing address lookup prior the endpoint call. Instead, the service locator will perform the address lookup internally on the caller's behalf, resulting the HTTP redirection to the correct address for the caller to follow.

Note that HTTP `307` works with other HTTP verbs, so the redirect works with HTTP Post with JSON payload or form parameters for example.

From the developer's perspective, this would mean the HTTP request's relative path and payload to the endpoint stays the same, only the base URI where the endpoint resides will be different.

From the application's perspective, the Service Locator Base URL will be provided by the `SERVICE_LOCATOR` environment variable when running within ConductR. When `SERVICE_LOCATOR` environment is present, configure the base URL of the endpoint by appending the endpoint name to the `SERVICE_LOCATOR`. The HTTP request made to the base URL configured in this manner will be automatically redirected to the desired endpoint.

If the `SERVICE_LOCATOR` environment is not present, the base URL of the endpoint can fallback to a default value. This will be useful for running the caller on development environment, for example.

#### Consolidated logging

Looking at the application log files are part of the regular support activities. With applications built in microservice fashion, the number of log files to be inspected can grow significantly. The effort to inspect and trace these log files will grow significantly since each of these log files is located in separate machines.

ConductR provides an out-of-the-box solution to collect and consolidate the logs generated by the application deployed and launched through ConductR itself.

Once consolidated, the logs then can be viewed using `conduct logs` command.

Let's view the log from the `visualizer` bundle by running the following command.

```bash
conduct logs visualizer
```

You should see the log entries from the `visualizer` application similar to the following. The `Listening for HTTP on /192.168.10.2:10609` entry indicates the `visualizer` application is started and bound to the `192.168.10.2` address. Since `192.168.10.2` is the address alias which points to our own local machine, note that `HOST` column will always be populated by the host address of our local machine. In the example below `Felixs-MBP-2` is the name of the local machine where `visualizer` is running on.

```
$ conduct logs visualizer
TIME                          HOST          LOG
Fri 2017-03-24T14:05:34+1100  Felixs-MBP-2  [warn] application - application.conf @ file:/Users/felixsatyaputra/.conductr/images/tmp/conductr-agent/192.168.10.2/bundles/73595ecbdad1f01a05db5304046b4ad5/execution-0-6241958403477423379/73595ecbdad1f01a05db5304046b4ad5c0a98ef83e201e507a153cea21087078/visualizer/conf/application.conf: 13: application.secret is deprecated, use play.crypto.secret instead
Fri 2017-03-24T14:05:34+1100  Felixs-MBP-2  [info] play.api.Play - Application started (Prod)
Fri 2017-03-24T14:05:34+1100  Felixs-MBP-2  [info] application - Signalled start to ConductR
Fri 2017-03-24T14:05:34+1100  Felixs-MBP-2  [info] p.c.s.NettyServer - Listening for HTTP on /192.168.10.2:10609
```

To see consolidated logging feature in action, scale the `visualizer` to `3` instances.

```bash
conduct run visualizer --scale 3
```

Once scaled to `3` instances, view the logs consolidated from all the `visualizer` instances by executing the following command.

```bash
conduct logs visualizer
```

You should see something similar to the following. The log entries are consolidated from all `3` instances of `visualizer` running on `192.168.10.1`, `192.168.10.2`, and `192.168.10.3`.

```
$ conduct logs visualizer
TIME                          HOST          LOG
Fri 2017-03-24T14:05:34+1100  Felixs-MBP-2  [info] application - Signalled start to ConductR
Fri 2017-03-24T14:05:34+1100  Felixs-MBP-2  [info] p.c.s.NettyServer - Listening for HTTP on /192.168.10.2:10609
Fri 2017-03-24T14:16:31+1100  Felixs-MBP-2  [warn] application - application.conf @ file:/Users/felixsatyaputra/.conductr/images/tmp/conductr-agent/192.168.10.3/bundles/73595ecbdad1f01a05db5304046b4ad5/execution-0-842840413817889011/73595ecbdad1f01a05db5304046b4ad5c0a98ef83e201e507a153cea21087078/visualizer/conf/application.conf: 13: application.secret is deprecated, use play.crypto.secret instead
Fri 2017-03-24T14:16:31+1100  Felixs-MBP-2  [info] play.api.Play - Application started (Prod)
Fri 2017-03-24T14:16:31+1100  Felixs-MBP-2  [info] application - Signalled start to ConductR
Fri 2017-03-24T14:16:31+1100  Felixs-MBP-2  [info] p.c.s.NettyServer - Listening for HTTP on /192.168.10.3:10166
Fri 2017-03-24T14:16:35+1100  Felixs-MBP-2  [warn] application - application.conf @ file:/Users/felixsatyaputra/.conductr/images/tmp/conductr-agent/192.168.10.1/bundles/73595ecbdad1f01a05db5304046b4ad5/execution-1-533158274229295787/73595ecbdad1f01a05db5304046b4ad5c0a98ef83e201e507a153cea21087078/visualizer/conf/application.conf: 13: application.secret is deprecated, use play.crypto.secret instead
Fri 2017-03-24T14:16:35+1100  Felixs-MBP-2  [info] play.api.Play - Application started (Prod)
Fri 2017-03-24T14:16:35+1100  Felixs-MBP-2  [info] application - Signalled start to ConductR
Fri 2017-03-24T14:16:35+1100  Felixs-MBP-2  [info] p.c.s.NettyServer - Listening for HTTP on /192.168.10.1:10822
```

The logs collected by ConductR is structured according to Syslog's definition of structured data. This structure is discussed in a greater detail in the [Logging Structure](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#Logging-Structure) page.

The collected log entries can be emitted to either [Elasticsearch](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#Setting-up-Elasticsearch) or [RSYSLOG](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#Setting-up-RSYSLOG).

When Elasticsearch is enabled, the log entries are indexed and thus they became searchable. The [Kibana UI](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#Setting-up-Kibana) can be installed to provide user interface for querying these log entries.

ConductR consolidated logging works with both an existing Elasticsearch cluster [outside of the Enterprise Suite](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#External-Elasticsearch-cluster), or the Elasticsearch cluster [managed by the Enterprise Suite](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#Elasticsearch-on-Standalone-ConductR-cluster).

By default, the Developer Sandbox starts up with a in-memory version of Elasticsearch called `eslite` to be used for development purposes. To enable the actual Elasticsearch on the Developer Sandbox, provide the `-f logging` option when executing `sandbox run`. The `-f logging` option will also enable Docker-based Kibana UI accessible through `http://192.168.10.1:5601`.

If you wish to see the actual Elasticsearch bundle in action, execute the following command. The Sandbox Elasticsearch instance is configured with the JVM heap sized to `1GB`. Ensure your machine has sufficient memory resource to run Elasticsearch with all Lagom Chirper services.

Maven:
```bash
sandbox run 2.0.0 -n 3 -f visualization -f logging
./mvn-install
```

SBT:
```bash
sandbox run 2.0.0 -n 3 -f visualization -f logging
sbt install
```

When using RSYSLOG, apart from directing the logs into the RSYSLOG logging service, the logs can be sent to any log aggregator that speaks syslog protocol. An example of such aggregator is [Papertrail](http://conductr.lightbend.com/docs/2.0.x/ConsolidatedLogging#Setting-up-Papertrail).

#### Network partition resilience

Due to its distributed nature, network partition is one of many failure scenarios that microservice based application has to contend with.

Network partition occurs when parts of the network is intermittently reachable, or unreachable due to network connectivity issues. The possibility of network partition occurring is quite real, especially when deploying to public cloud infrastructure where there is limited control of the underlying network infrastructure.

Often for the purpose of resiliency and performance, multiple instances of a service are started and its state is synchronized by clustering the instances together. When network partition occurs, one or more instances of these services are separated from the rest of the network. Therefore the updates to the cluster state may not reach these orphaned instances, which may lead to inconsistencies or corruption of data managed by these service instances.

ConductR provides an out of the box defense against network partition in the form of Akka Split Brain Resolver (SBR) which is one of the commercial extension available from [Akka](http://akka.io/).

ConductR comprises of core nodes and agent nodes. The core nodes contains the state of the applications managed by ConductR, i.e. where it's running, how many instances are requested, and whether the number of requested instances has been met. The core node is also responsible for the decision making related to scaling up and down of application instances. The agent nodes are responsible for the actual starting and stopping of the applications.

Upon encountering the network partition, the segment of the network which contains majority of the core node will be kept running. The remaining instances of core nodes will automatically restart itself, waiting for the opportunity to rejoin the cluster. Once the opportunity to rejoin the cluster is available (i.e. network failure is now fixed), the correct state of the applications managed by ConductR will be replicated to these core nodes.

When agent nodes encounters the network partition, each of the agent node will attempt to reconnect to core nodes on other parts of the network automatically. If the attempt to reconnect actually failed after a given period of time, the agent node will shut itself down along with all the application processes that it manages. Agent nodes will shutdown the application processes to prevent divergent application state that may occur during network partition. Once clean shutdown is accomplished, the agent node will restart and attempt to automatically reconnect to all the core nodes it has seen previously. Once it's able to rejoin the core node, if the number of application instances has not been met, the core node will instruct the agent to start the processes accordingly.

Restart sandbox with 3 instances of ConductR core. Note the option `-n 3:3` which indicates `3` core instances and `3` agent instances.

```bash
sandbox run 2.0.0 -n 3:3
```

Redeploy the Lagom Chirper example.

Maven:
```bash
./mvn-install
```

SBT:
```bash
sbt install
```

Let's scale `Front-End` to `3` instances.

```bash
conduct run front-end --scale 3
```

Once done, the state should look similar to the following.

```
Felixs-MacBook-Pro-2:lagom-java-chirper-example felixsatyaputra$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
acc2d2b          friend-impl            v1     3     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     3     0     1  haproxy
3349b6b          eslite                 v1     3     0     1  elasticsearch
188f510          chirp-impl             v1     3     0     1  web
f1c7210          load-test-impl         v1     3     0     1  web
93d0f25-44e4d55  front-end              v1     3     0     3  web
e643e4a          activity-stream-impl   v1     3     0     1  web
1acac1d          cassandra              v3     3     0     1  cassandra
```

Execute the following command to confirm the number of core node instances.

```bash
conduct members
```

The output should be similar to the following, i.e. there should be `3` core node running.

```
$ conduct members
UID          ADDRESS                                ROLES       STATUS  REACHABLE
-1775534087  akka.tcp://conductr@192.168.10.1:9004  replicator  Up            Yes
-56170110    akka.tcp://conductr@192.168.10.2:9004  replicator  Up            Yes
-322524621   akka.tcp://conductr@192.168.10.3:9004  replicator  Up            Yes
```

Execute the following command to confirm the number of core node instances.

```bash
conduct agents
```

The output should be similar to the following, i.e. there should be `3` agent instances running.

```
Felixs-MBP-2:lagom-java-chirper-example felixsatyaputra$ conduct agents
ADDRESS                                                                            ROLES                            OBSERVED BY
akka.tcp://conductr-agent@192.168.10.1:2552/user/reaper/cluster-client#1659172705  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.2:2552/user/reaper/cluster-client#-966725902  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.3:2552/user/reaper/cluster-client#1706931439  web    akka.tcp://conductr@192.168.10.3:9004
```

Due to the fact that core and agent instances are bound to addresses which are an address alias for loopback interface, the simplest way to simulate network partition is to pause the core and agent instances. When the signal `SIGSTOP` is issued to both core and agent instances, they will be paused and effectively frozen in time. From the perspective from other core and agent nodes, the frozen core and agent node has become unreachable, effectively simulating network partition from their point of view.

We'll pause the agent instance listening on `192.168.0.3` by executing the following command.

```
$ pgrep -f "conductr.ip=192.168.10.3" | xargs kill -s SIGSTOP
```

Similary, we'll pause the core instance listening on `192.168.0.3`.

```
$ pgrep -f "conductr.agent.ip=192.168.10.3" | xargs kill -s SIGSTOP
```

Monitor the member state by issuing a `watch` on `conduct members`. For those running on MacOS, the `watch` command can be installed via `brew` using `brew install watch`. For those on MacOS and don't wish to install watch, just issue the `conduct members` repeatedly.

```bash
$ watch conduct members
```

Eventually (this will take at least a minute to occur), we will see the member on `192.168.10.3` has become unreachable (i.e. `REACHABLE` is `No`).

```
UID          ADDRESS                                ROLES       STATUS  REACHABLE
-1775534087  akka.tcp://conductr@192.168.10.1:9004  replicator  Up            Yes
-56170110    akka.tcp://conductr@192.168.10.2:9004  replicator  Up            Yes
-322524621   akka.tcp://conductr@192.168.10.3:9004  replicator  Up             No
```

Eventually the member on `192.168.10.3` will be considered down by other members and will be removed from the member list.

```
UID          ADDRESS                                ROLES       STATUS  REACHABLE
-1775534087  akka.tcp://conductr@192.168.10.1:9004  replicator  Up            Yes
-56170110    akka.tcp://conductr@192.168.10.2:9004  replicator  Up            Yes
```

Similarly, either issue `watch` on `conduct agents`, or execute `conduct agents` repeatedly.

```bash
$ watch conduct agents
```

Eventually (this may require serveral minutes to complete), we will see the agent on `192.168.10.3` can no longer be observed by any remaining member.

```
ADDRESS                                                                            ROLES                            OBSERVED BY
akka.tcp://conductr-agent@192.168.10.1:2552/user/reaper/cluster-client#1659172705  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.2:2552/user/reaper/cluster-client#-966725902  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.3:2552/user/reaper/cluster-client#1706931439  web
```

Eventually the agent on `192.168.10.3` will be considered down by other members and will be removed from the member list.

```
ADDRESS                                                                            ROLES                            OBSERVED BY
akka.tcp://conductr-agent@192.168.10.1:2552/user/reaper/cluster-client#1659172705  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.2:2552/user/reaper/cluster-client#-966725902  web    akka.tcp://conductr@192.168.10.2:9004
```

Once this occur, issue `conduct info` to see the state of our cluster. The `#REP` column which indicates the replicated copy of the bundle file has been reduced from `3` to `2` due to the missing core node indicated by the `conduct members`. The `#RUN` column of the `front-end` has been reduced from `3` to `2` due to the missing agent indicated by the `conduct agents`.

```
$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
acc2d2b          friend-impl            v1     2     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     2     0     1  haproxy
3349b6b          eslite                 v1     2     0     1  elasticsearch
188f510          chirp-impl             v1     2     0     1  web
f1c7210          load-test-impl         v1     2     0     1  web
93d0f25-44e4d55  front-end              v1     2     0     2  web
e643e4a          activity-stream-impl   v1     2     0     1  web
1acac1d          cassandra              v3     2     0     1  cassandra
```

We'll unfreeze both core and agent instance on `192.168.10.3`.

```bash
pgrep -f "conductr.agent.ip=192.168.10.3" | xargs kill -s SIGCONT
pgrep -f "conductr.ip=192.168.10.3" | xargs kill -s SIGCONT
```

The core and agent instance on `192.168.10.3` will realize that it has been split from the cluster, and will automatically restart.

Eventually the `conduct members` will indicate a new core instance on `192.168.10.3` has rejoined the cluster. The new core instance is indicated by the new `UID` value of `-761520616` while the previous core instance has the value of `-322524621`.

```
UID          ADDRESS                                ROLES       STATUS  REACHABLE
-1775534087  akka.tcp://conductr@192.168.10.1:9004  replicator  Up            Yes
-56170110    akka.tcp://conductr@192.168.10.2:9004  replicator  Up            Yes
-761520616   akka.tcp://conductr@192.168.10.3:9004  replicator  Up            Yes
```

Similarly, the `conduct agents` will eventually indicate that the restarted agent had rejoined the cluster.

```
Felixs-MacBook-Pro-2:lagom-java-chirper-example felixsatyaputra$ conduct agents
ADDRESS                                                                            ROLES                            OBSERVED BY
akka.tcp://conductr-agent@192.168.10.1:2552/user/reaper/cluster-client#1659172705  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.2:2552/user/reaper/cluster-client#-966725902  web    akka.tcp://conductr@192.168.10.2:9004
akka.tcp://conductr-agent@192.168.10.3:2552/user/reaper/cluster-client#1706931439  web    akka.tcp://conductr@192.168.10.2:9004
```

And finally, the state of the cluster is restored as before with the `#REP` value of `3` and the `front-end` has recovered back to `3` instances.

```
$ conduct info
ID               NAME                  VER  #REP  #STR  #RUN  ROLES
acc2d2b          friend-impl            v1     3     0     1  web
bdfa43d-e5f3504  conductr-haproxy       v2     3     0     1  haproxy
3349b6b          eslite                 v1     3     0     1  elasticsearch
188f510          chirp-impl             v1     3     0     1  web
f1c7210          load-test-impl         v1     3     0     1  web
93d0f25-44e4d55  front-end              v1     3     0     3  web
e643e4a          activity-stream-impl   v1     3     0     1  web
1acac1d          cassandra              v3     3     0     1  cassandra
```

ConductR provides a self-healing capability when encountering network partition. Self-healing means the recovery from network partition is automatic, relieving operations from having to detect when a network partition has occured, decide which network segment to keep, and having to quickly restart multitude of instances in response to the failure.

We believe this is one of the compelling reason why you should give ConductR a try.

## Next Steps

At this point you have deployed Lagom Chirper into the Developer Sandbox, and familiarized yourself with the feature provided by ConductR.

We hope that we will have sparked your interest in trying out Developer Sandbox with the applications you have written. Please head to the [documentation](https://conductr.lightbend.com/docs) for further operational and development information.

Thank you!
