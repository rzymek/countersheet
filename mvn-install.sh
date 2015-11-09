#!/bin/bash 
mkdir target ; cd target
set -e
wget -c --content-disposition -O vassal.zip http://sourceforge.net/projects/vassalengine/files/latest/download
unzip vassal.zip
version=`echo VASSAL-*/ | cut -d- -f2 | cut -d/ -f1`
Vengine=`echo VASSAL-*/lib/Vengine.jar`
echo '<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>org.vassal</groupId>
	<artifactId>Vengine</artifactId>
	<version>'$version'</version>

	<dependencies>' > vengine.pom.xml

for f in `ls -w1 VASSAL-*/lib/*.jar |grep -v Vengine.jar`;do
	artifact=`basename $f .jar`
	mvn install:install-file -Dfile=$f -DgroupId=org.vassal -DartifactId=$artifact -Dversion=$version -Dpackaging=jar
	echo '
		<dependency>
			<groupId>org.vassal</groupId>
			<artifactId>'$artifact'</artifactId>
			<version>'$version'</version>
		</dependency>' >> vengine.pom.xml
done
echo '</dependencies>
</project>' >> vengine.pom.xml
mvn install:install-file -Dfile=$Vengine -DgroupId=org.vassal -DartifactId=Vengine -Dversion=$version -Dpackaging=jar -DpomFile=vengine.pom.xml
