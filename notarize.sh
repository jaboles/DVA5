#!/bin/bash

notarizebinary=''
if [[ $1 =~ "app" ]]; then
	rm notarize-$1.zip
	zip -r notarize-$1.zip $1
	notarizebinary="notarize-$1.zip"
else
	notarizebinary=$1
fi

password=''
if [[ "$SYSTEM_COLLECTIONURI" == "https://jonathanboles.visualstudio.com/" ]]; then
  password="@env:DVANotarizationPassword"
else
  password="@keychain:DVAnotarization"
fi

echo Sending $notarizebinary to Apple
output=`xcrun altool --notarize-app -f $notarizebinary --primary-bundle-id jb.dva -t osx -u jonno@fastmail.fm -p $password 2>&1`
echo output is ${output}
uuid=`echo ${output} | tail -1 | rev | cut -d' ' -f1 | rev`
echo UUID is ${uuid}
while true; do
	output=`xcrun altool --notarization-info $uuid -u jonno@fastmail.fm -p $password 2>&1`
	echo $output
	if [[ $output =~ "Status: success" ]]; then
		break
	fi
	sleep 15
done

if [[ $notarizebinary =~ "notarize-" ]]; then
	rm $notarizebinary
fi

xcrun stapler staple $1
