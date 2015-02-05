=============================================================
DmsUtils Project
=============================================================

DmsUtils is a shot Api that lets you interact with different content Repositories. 
It provides a common interface with several methods that can be used to interact with several content repositories.

It provides support for structured and unstructured content, versioning and more.


Building DmsUtils
===================

You can build DmsUtils by simply run a Maven command, together with the wanted profile:

   Ex:  mvn clean install -Dmaven.test.skip=true 

You need Maven 2.1 (or higher, Maven 3 recommended) with Java 7 (or higher) for the build. 


Jackrabbit Repository Usage
=============================================================

Usage the Jackrabbit Repository can be done in 3 ways:
- using RMI - more information at: <http://jackrabbit.apache.org/jackrabbit-jcr-rmi.html>
- using JNDI and Apache Jackrabbit JCA Resource Adapter - more information ar: <http://jackrabbit.apache.org/jackrabbit-jca-resource-adapter.html>
- using directly repository.xml stored locally and create a new Transient Repository based on this. repository.xml file should be stored in a separate repository and referenced in the jcr.properties file