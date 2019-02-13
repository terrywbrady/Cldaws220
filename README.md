## CLDAWS 220 Files

TODO: modify maven to deploy jsp file appropriately.

[dns]:8080/app/?url=http://www.textfiles.com/etext/FICTION/warpeace.txt&count=20

## Actions required

- Create EC2 with Amazon Linux
- Install Java 8: https://serverfault.com/questions/664643/how-can-i-upgrade-to-java-1-8-on-an-amazon-linux-server?newreg=c859f7fa6cd84574ad4cd9438fcb5b4f
- Install Jetty
- Configure Jetty App (done manually on Cloud9)
  - deploy.ini
  - http.ini
  - jsp.ini

## Server startup script

```
#!/bin/bash

# Update OS software
sudo -n yum -y update

# Install Java 8
sudo -n yum -y install java-1.8.0-openjdk-devel
sudo -n yum -y remove java-1.7.0-openjdk

# Install Git
sudo -n yum -y install git

# Install Jetty
JETTY=https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.14.v20181114/jetty-distribution-9.4.14.v20181114.zip
curl -o jetty.zip $JETTY
unzip jetty.zip
mv jetty-distribution* /home/ec2-user/jetty
export JETTY_HOME=/home/ec2-user/jetty

# Install Maven
export MAVEN=http://apache.osuosl.org/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip
curl -o maven.zip $MAVEN
unzip maven.zip
mv apache-maven* /home/ec2-user
export PATH=$PATH:/home/ec2-user/apache-maven-3.6.0/bin

# Set Useful Env Vars
echo "export PATH=$PATH" >> /home/ec2-user/.bashrc
echo "export JETTY_HOME=$JETTY_HOME" >> /home/ec2-user/.bashrc

# Create Web App Runtime Dir
mkdir /home/ec2-user/awsProj
cd /home/ec2-user/awsProj
java -jar $JETTY_HOME/start.jar --create-startd --add-to-start=jsp,http,deploy
mkdir webapps
cd webapps
mkdir wordCount
cd wordCount
mkdir WEB-INF
cd WEB-INF
mkdir lib

# Clone Project Code
cd /home/ec2-user
git clone https://github.com/terrywbrady/CldAws220.git

# Build Code
cd CldAws220
mvn install

# Install Code to server
cd /home/ec2-user/awsProj/webapps/wordCount
cp /home/ec2-user/CldAws220/src/main/resources/* .
cp /home/ec2-user/CldAws220/target/*.jar WEB-INF/lib

# Set startup scripts

```

## Project Build
```
git clone https://github.com/terrywbrady/CldAws220.git
cd CldAws220
mvn install

mkdir ~/awsProj
cd awsProj
java -jar $JETTY_HOME/start.jar --create-startd --add-to-start=jsp,http,deploy
mkdir webapps
cd webapps
mkdir wordCount
cd wordCount
mkdir WEB-INF
cd WEB-INF
mkdir lib
cp /home/ec2-user/CldAws220/src/main/resources/* .
cp /home/ec2-user/CldAws220/target/*.jar WEB-INF/lib
```

## project rebuild
```
cd /home/ec2-user/CldAws220
git pull origin
mvn install
```

### project update

```
cd /home/ec2-user/awsProj/webapps/wordCount
cp /home/ec2-user/CldAws220/src/main/resources/* .
cp /home/ec2-user/CldAws220/target/*.jar WEB-INF/lib
```


### project update Cloud9

```
cd /home/ec2-user/awsProj/webapps/wordCount
cp /home/ec2-user/environment/CldAws220/src/main/resources/* .
cp /home/ec2-user/environment/CldAws220/target/*.jar WEB-INF/lib
```
## Enable Service startup

`sudo vi /etc/rc.local`

Add the following

```
export JETTY_HOME=/home/ec2-user/jetty
cd /home/ec2-user/awsProj
java -jar $JETTY_HOME/start.jar
```

## Message Processor

```
cd /home/ec2-user/CldAws220
java -jar target/CldAws220*.jar
```
