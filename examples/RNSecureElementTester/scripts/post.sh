#!/bin/sh

cd ios;

if [ $(uname -s) = 'Darwin' ]; then
  pod install
else echo 'Skipping pod installation since we are not Mac OS'; fi
