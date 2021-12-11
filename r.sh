#!/bin/bash
pushd build/Output/DVA.app/Contents/MacOS/ >/dev/null
./dva "$@"
popd >/dev/null
