#!/bin/bash
for files in *.wav
do
 number=`echo $files|cut -f 1 -d ' '`
 newfilename=`echo $files|cut -f 2- -d ' '`
 #echo $number
 #echo $newfilename
 if [ "$files" != "$newfilename" ] && [[ "$number" =~ ^[0-9]+$ ]] && [ "$number" -gt 1000 ] ; then
  suffix=
  #if [ "$number" -ge 7000 ] && [ "$number" -lt 10000 ] ; then
  # suffix=.f
  #elif [ "$number" -ge 17000 ] && [ "$number" -lt 20000 ] ; then
  # suffix=.f
  #elif [ "$number" -ge 27000 ] && [ "$number" -lt 30000 ] ; then
  # suffix=.f
  #fi
  newbasename=$(basename "$newfilename")
  newextension=${newfilename##*.}
  newbasename=${newbasename%.*}
  newfilename=$newbasename$suffix.$newextension
  echo "Rename $files to $newfilename"
  mv "$files" "$newfilename"
 fi
done

