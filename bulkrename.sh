#!/bin/bash
for files in *.$1
do
 echo "$files" "${files%.$1}.tmp"
 mv "$files" "${files%.$1}.tmp"
done
for files in *.tmp
do
 echo "$files" "${files%.tmp}.$2"
 mv "$files" "${files%.tmp}.$2"
done
