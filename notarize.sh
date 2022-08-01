#!/bin/bash

password=''
if [[ "$SYSTEM_COLLECTIONURI" == "https://jonathanboles.visualstudio.com/" ]]; then
  ./notarize1.sh $*
else
  ./notarize2.sh $*
fi
