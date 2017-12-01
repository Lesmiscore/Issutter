#!/bin/bash

# This script used to pre-download all dependencies.

if groovy -e "$(cat *.groovy | grep Grab) import groovy.*" then;
  echo "Dependencies are properly downloaded. You can go ahead"
else
  echo -n "Dependencies may not be downloaded correctly.\nReport about above."
fi
