# Postgres Database Setup

## Procedure

1. Install the service binding operator using the OCP console.

2. Create a pgo project. This is where you will install the crunchy operator,

```
oc project pgo
```

3. Clone the postgres operator repo and cd into it. 

```
git clone -b v4.4.1 https://github.com/CrunchyData/postgres-operator.git
cd postgres-operator
```

4. (*) Update conf/postgres-operator/pgo.yaml to specify the storage classes to be used. For example:

```
- StorageClass:  fast … with:
+    StorageClass: rook-ceph-cephfs-internal
- StorageClass:  rook-ceph-block. … with:
+    StorageClass:  rook-ceph-block-internal
```

5. Create postgress resources.

```
kubectl -n "$PGO_OPERATOR_NAMESPACE" create configmap pgo-config --from-file=./conf/postgres-operator
kubectl -n "$PGO_OPERATOR_NAMESPACE" create secret generic pgo-backrest-repo-config  --from-file=config=./conf/pgo-backrest-repo/config --from-file=sshd_config=./conf/pgo-backrest-repo/sshd_config
```

6. Install the crunchy operator in the pgo namespace using the OCP console.

7. Run the ./deploy/install-bootstrap-creds.sh

```
PGO_CMD=kubectl ./deploy/install-bootstrap-creds.sh
secret/pgorole-pgoadmin created
secret/pgorole-pgoadmin labeled
secret/pgouser-admin created
secret/pgouser-admin labeled
```

8. Download the pgo command and needed cert to run it. Be sure update your .bash_profile with the exports
it outputs if you plan to come back to it later using a different shell session.

```
PGO_CMD=kubectl ./installers/kubectl/client-setup.sh
pgo client files have been generated, please add the following to your bashrc and execute them now:
export PATH=/root/.pgo/pgo:$PATH
export PGOUSER=/root/.pgo/pgo/pgouser
export PGO_CA_CERT=/root/.pgo/pgo/client.crt
export PGO_CLIENT_CERT=/root/.pgo/pgo/client.crt
export PGO_CLIENT_KEY=/root/.pgo/pgo/client.key
```

9. (*)Create a Route for external access to the postgress operator deployment.

For this step follow the instructions on the deployed operator main page. It allows for the creation of a route:

```
oc -n "$PGO_OPERATOR_NAMESPACE" expose deployment postgres-operator
service/postgres-operator exposed
```
```
oc -n "$PGO_OPERATOR_NAMESPACE" create route passthrough postgres-operator --service=postgres-operator
route.route.openshift.io/postgres-operator created
```
```
export PGO_APISERVER_URL="https://$(oc -n "$PGO_OPERATOR_NAMESPACE" get route postgres-operator -o jsonpath="{.spec.host}")"
```
```
echo $PGO_APISERVER_URL
https://<ROUTE URL>
```

10. Prep for application deployment.

```
pgo create namespace service-binding-demo
created namespace service-binding-demo
```

```
pgo create cluster my-demo-db -n service-binding-demo
created cluster: my-demo-db
workflow id: 8bad7968-4a8c-47f4-b280-7d007a15ff7f
database name: my-demo-db
users:
	username: testuser password: xUgXBVRF:[?;pLwbOWCo+dEk
```

11. Setup the JPA Starter. 

```
oc project service-binding-demo
Now using project "service-binding-demo" on server "https://<YOUR CLUSTER URL>:6443".
```

Create a directory for the starter and create it.

```
mkdir jpastarter
cd jpastarter
odo create java-openliberty mysboproj --starter
```

**TEMPORARY WORKAROUD FOR DEV TEST:**

a. Update devfile.yaml

Update 1: Use the new/dev starter link. The update should look like this:

```
starterProjects:
  - name: user-app
    git:
      remotes:
        origin: 'https://github.com/mezarin/application-stack-starters-test.git'
```

Update 2: (*) The build exec should look like this:

```
                       echo "will run the devBuild command" && echo "...moving liberty"
                                                            && mkdir -p /projects/target/liberty
                                                            && mv /opt/ol/wlp /projects/target/liberty
                                                            && mvn -Dmaven.repo.local=/mvn/repository package
                                                            && touch ./.disable-bld-cmd;
```

Basically move `&& mv /opt/ol/wlp /projects/target/liberty` above the mvn execution

b. Delete all files except for devfile.yaml and run these commands:

```
odo create myjpa --starter
```
```
odo url list
Found the following URLs for component mysboproj
NAME     STATE          URL     PORT     SECURE     KIND
ep1      Not Pushed     ://     9080     false      route
```
```
odo push
```
``` 
odo url list
Found the following URLs for component mysboproj
NAME     STATE      URL                                                                     PORT     SECURE     KIND
ep1      Pushed     http://<APP ROUTE URL>     9080     false      route
```

12. (*) Give service accounts under project service-binding-demo access to privileged SCC.

```
oc adm policy add-scc-to-group privileged system:serviceaccounts:service-binding-demo
clusterrole.rbac.authorization.k8s.io/system:openshift:scc:privileged added: "system:serviceaccounts:service-binding-demo"
```

13. Bind the application to the database.

```
odo catalog list services
Operators available in the cluster
NAME                                    CRDs
service-binding-operator.v0.1.1-364     ServiceBindingRequest
```
```
odo service create service-binding-operator.v0.1.1-364/ServiceBindingRequest
Deploying service of type: ServiceBindingRequest
 ✓  Deploying service [39ms]
 ✓  Service '' was created

Progress of the provisioning will not be reported and might take a long time
You can see the current status by executing 'odo service list'
```
```
odo push
```

Link the application to the Postgres service. This creates an empty secret called: mysboproj-servicebindingrequest-example-servicebindingrequest.
It is not until you update the actual service CR instance that the DB key/value pairs are created. 

```
odo link ServiceBindingRequest/example-servicebindingrequest
 ✓  Successfully created link between component "mysboproj" and service "ServiceBindingRequest/example-servicebindingrequest"

To apply the link, please use `odo push`
```
```
odo service list
NAME                                                                                    AGE
ServiceBindingRequest/example-servicebindingrequest                                     2m13s
ServiceBindingRequest/mysboproj-servicebindingrequest-example-servicebindingrequest     8s
```
```
odo push
```

``` 
oc get servicebindingrequest mysboproj-servicebindingrequest-example-servicebindingrequest
NAME                                                            AGE
mysboproj-servicebindingrequest-example-servicebindingrequest   7m5s
```

Add the following to the annotations section in pgcluster CRD:
```
    servicebindingoperator.redhat.io/spec.database: 'binding:env:attribute'
    servicebindingoperator.redhat.io/spec.namespace: 'binding:env:attribute'
    servicebindingoperator.redhat.io/spec.port: 'binding:env:attribute'
    servicebindingoperator.redhat.io/spec.usersecretname-password: 'binding:env:object:secret'
    servicebindingoperator.redhat.io/spec.usersecretname-username: 'binding:env:object:secret'
```
It should look like this when done:
```
apiVersion: crunchydata.com/v1
kind: Pgcluster
metadata:
  annotations:
    current-primary: my-demo-db
    primary-deployment: my-demo-db
    servicebindingoperator.redhat.io/spec.database: 'binding:env:attribute'
    servicebindingoperator.redhat.io/spec.namespace: 'binding:env:attribute'
    servicebindingoperator.redhat.io/spec.port: 'binding:env:attribute'
    servicebindingoperator.redhat.io/spec.usersecretname-password: 'binding:env:object:secret'
    servicebindingoperator.redhat.io/spec.usersecretname-username: 'binding:env:object:secret'
```

Replace the following in the service binding CR instance. This populates mysboproj-servicebindingrequest-example-servicebindingrequest with the information needed by the Liberty server's 
datasource config.

From:
```
  backingServiceSelector:
    group: apps.openshift.io
    kind: ServiceBindingRequest
    namespace: service-binding-demo
    resourceRef: example-servicebindingrequest
    version: v1alpha1
```
To
```
  backingServiceSelectors:
    - group: crunchydata.com
      kind: Pgcluster
      namespace: service-binding-demo
      resourceRef: my-demo-db
      version: v1
``` 
```
odo push -f 
```

That is all!

**NOTE:**

1. In the end you should have:

```
oc get pods -n service-binding-demo
NAME                                              READY   STATUS      RESTARTS   AGE
backrest-backup-my-demo-db-ptbrr                  1/1     Running     0          5m36s
my-demo-db-64f65f7fb8-qzwsw                       1/1     Running     0          8m2s
my-demo-db-backrest-shared-repo-9887df576-5lf2h   1/1     Running     0          8m53s
my-demo-db-stanza-create-hxj4f                    0/1     Completed   0          6m21s
mysboproj-6b5f6cfd55-kfpgs                        1/1     Running     0          4m9s
```

2. Once the pods are fully available get the route host add the correct URI to it:

```
odo url list
Found the following URLs for component mysboproj
NAME     STATE      URL                        PORT     SECURE     KIND
ep1      Pushed     http://<APP ROUTE URL>     9080     false      route
```

3. The URL below should give you a http 204 status code (NO CONTENT):
```
http://<APP ROUTE URL>/
```
4. The URL below should give you a readiness health check output:
```
http://<APP ROUTE URL>/health

Output:

checks	
0	
data	
databaseProductVersion	"12.4"
databaseProductName	"PostgreSQL"
driverVersion	"42.1.1"
connectionStatus	"Successfully validated"
driverName	"PostgreSQL JDBC Driver"
name	"DatabaseReadinessCheck"
status	"UP"
status	"UP"
```

(*) Items that needed to be done that are not currently documented but needed for this to work properly.