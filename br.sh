#!/bin/bash
ant DevBuild -Dfastbuild=1
./r.sh "$@"
