#!/bin/bash
for files in *.mp3
do
 equivalentWav=${files%.mp3}.wav
 if [ -f "$equivalentWav" ] ; then
	echo "$equivalentWav exists"
    #afplay "$equivalentWav"
    #afplay "$files"
	#if [ -f "${files%.mp3}.f.mp3" ] ; then
	#	afplay "${files%.mp3}.f.mp3"
    #   afplay "$equivalentWav"
	#fi
	#read -p "[D]elete wav, or [M]ove to replaced sounds folderm [I]gnore?" dm
	#dm=m
    #case $dm in
    #    [Dd]* ) svn delete "$equivalentWav";;
    #    [Mm]* ) svn rename "$equivalentWav" "../Sydney-Male (replaced low-quality sounds)/$equivalentWav";;
	#	[Ii]* ) echo "Ignored";;
    #    * ) echo "Please answer yes or no.";;
    #esac
	if [ -f "../Sydney-Male (replaced low-quality sounds)/$equivalentWav" ] ; then
		svn revert "$equivalentWav"
	else
		echo "$equivalentWav was replaced but is not in the replacement folder"
		afplay "$equivalentWav"
		afplay "$files"
		read -p "[K]eep in replacement folder, or leave [D]eleted, or [I]gnore?" kd
		case $kd in
		    [Kk]* ) svn rename "$equivalentWav" "../Sydney-Male (replaced low-quality sounds)/$equivalentWav";;
		    [Dd]* ) svn revert "$equivalentWav";;
			[Ii]* ) echo "Ignored";;
		    * ) echo "Please answer yes or no.";;
		esac
	fi
 fi
done
