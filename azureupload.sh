#!/bin/bash
java -cp build/Debug/DVA.jar:jars/azure-storage-2.2.0.jar:jars/slf4j-api-1.7.7.jar:jars/slf4j-jdk14-1.7.7.jar:jars/collections-query-0.2.9.jar:retrolambda-2.0.6.jar jb.dvacommon.WAzureUpdater $*
