#!/bin/bash
replacementFolder=Sydney-Female\ \(replaced\ low-quality\ sounds\)
inp=
for files in *.wav
do
 equivalentMp3=${files%.wav}.mp3
 equivalentWavF=${files%.wav}.f.wav
 if [ -f "$equivalentMp3" ] ; then
    inp=p
    while [[ "$inp" == "p" ]] ; do
	inp=blah
	echo "$equivalentMp3 exists"

	if [ -f "../$replacementFolder/$equivalentMp3" ] ; then
		echo "  $equivalentMp3 is in the replacement folder"
		svn delete "$equivalentMp3"

	else
		echo "  mp3"
		afplay "$equivalentMp3"
		#echo "  wav"
		#afplay "$files"
		#if [ -f "$equivalentWavF" ] ; then
		#	echo "  wav F"
		#	afplay "$equivalentWavF"
		#fi
		echo "  replaceable"

		echo "$equivalentMp3 is not in the replacement folder"
		read -p "[K]eep in replacement folder, [D]elete, [I]gnore or re[p]eat?" kd
		case $kd in
		    [k]* ) svn rename "$equivalentMp3" "../$replacementFolder/$equivalentMp3";;
		    [d]* ) svn delete "$equivalentMp3";;
		    [i]* ) echo "Ignored";;
		    [p]* ) echo "Repeating"; inp=p;;
		    * ) echo "Please answer yes or no.";;
		esac
	fi
     done
 fi
done
