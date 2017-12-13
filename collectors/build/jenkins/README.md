# Hygieia Build Collectors / Jenkins

This project uses Spring Boot to package the collector as an executable JAR with dependencies.

## Building and Deploying

To package the collector into an executable JAR file, run:
```bash
mvn install
```

Copy this file to your server and launch it using:
```bash
java -JAR hudson-collector.jar
```

## application.properties

You will need to provide an **application.properties** file that contains information about how to connect to the Dashboard MongoDB database instance, as well as properties the Hudson collector requires. See the Spring Boot [documentation](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-external-config-application-property-files) for information about sourcing this properties file.

### Sample application.properties file

```properties
# Database Name
dbname=dashboard

# Database HostName - default is localhost
dbhost=localhost

# Database Port - default is 27017
dbport=9999

# MongoDB replicaset
dbreplicaset=[false if you are not using MongoDB replicaset]
dbhostport=[host1:port1,host2:port2,host3:port3]

# Database Username - default is blank
dbusername=db

# Database Password - default is blank
dbpassword=dbpass

# Collector schedule (required)
jenkins.cron=0 0/5 * * * *

# The page size
jenkins.pageSize=1000

# The folder depth - default is 10
jenkins.folderDepth=10

# Jenkins server (required) - Can provide multiple
jenkins.servers[0]=http://jenkins.company.com

# If using username/token for api authentication
#   (required for Cloudbees Jenkins Ops Center) see sample
jenkins.servers[1]=http://username:token@jenkins.company.com

# Another option: If using same username/password Jenkins auth,
#   set username/apiKey to use HTTP Basic Auth (blank=no auth)
jenkins.usernames[0]=
jenkins.apiKeys[0]=

# Determines if build console log is collected - defaults to false
jenkins.saveLog=true
```
