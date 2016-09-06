#!/bin/bash
if [ ! -d build/soundjars ]; then
	mkdir build/soundjars
fi
pushd sounds >/dev/null
for i in *
do
	pushd "$i" >/dev/null
	JAR=../../build/soundjars/$i.jar
    ls -1 | sed -e 's/.*/\"&\"/' > $TMPDIR/files.list~
	
    if [ ! -f files.list ]; then
        REGEN=2
	elif ! cmp -b files.list $TMPDIR/files.list~; then
    	REGEN=2
    elif [ ! -f "$JAR" ]; then
        REGEN=1
    else
        REGEN=0
    fi
	
	if [ $REGEN -gt 0 ]; then
	    echo "Regenerating $i"
	    if [ $REGEN -gt 1 ]; then
	        mv $TMPDIR/files.list~ files.list
	    fi
	    pwd
        jar c0vfM "$JAR" @files.list
    else
        echo "Skipping $i"
        rm $TMPDIR/files.list~
	fi
    touch -r files.list "$JAR"
	popd >/dev/null
done
popd >/dev/null
