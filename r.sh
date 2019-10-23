#!/bin/bash
pushd build/Debug/DVA.app/Contents/MacOS/ >/dev/null
./dva "$@"
popd >/dev/null
