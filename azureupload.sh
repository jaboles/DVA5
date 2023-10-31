#!/bin/bash
java -cp build/Output/DVA.jar:jars/azure-storage-2.2.0.jar:jars/log4j-api-2.20.0.jar:jars/log4j-core-2.20.0.jar jb.dvacommon.WAzureUpdater $*
