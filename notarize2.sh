#!/bin/bash

notarizebinary=''
if [[ $1 =~ "app" ]]; then
	rm notarize-$1.zip
	zip -r notarize-$1.zip $1
	notarizebinary="notarize-$1.zip"
else
	notarizebinary=$1
fi

echo Sending $notarizebinary to Apple
output=''
if [[ "$SYSTEM_COLLECTIONURI" == "https://jonathanboles.visualstudio.com/" ]]; then
  output=`xcrun notarytool submit $notarizebinary --wait --apple-id jonno@fastmail.fm --team-id W2WXX3D2U6 --password $DVANOTARIZATIONPASSWORD 2>&1`
else
  output=`xcrun notarytool submit $notarizebinary --wait --keychain-profile DVANotarization2 2>&1`
fi
echo output is ${output}

if [[ $notarizebinary =~ "notarize-" ]]; then
	rm $notarizebinary
fi

xcrun stapler staple $1
