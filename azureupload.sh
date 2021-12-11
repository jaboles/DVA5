#!/bin/bash
java -cp build/Output/DVA.jar:jars/azure-storage-2.2.0.jar:jars/log4j-api-2.14.1.jar:jars/log4j-core-2.14.1.jar jb.dvacommon.WAzureUpdater $*
