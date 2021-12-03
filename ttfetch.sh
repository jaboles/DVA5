#!/bin/bash
java -cp build/Debug/DVA.jar:jars/log4j-api-2.14.1.jar:jars/log4j-core-2.14.1.jar jb.plasma.gtfs.GtfsGenerator $*
