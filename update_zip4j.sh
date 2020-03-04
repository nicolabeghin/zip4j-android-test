#!/bin/bash

TARGET_FOLDER=zip4j_updated
LIBRARY_FOLDER=zip4j
REPO=git@github.com:srikanth-lingala/zip4j.git

# clone new version from official repo
git clone $REPO $TARGET_FOLDER
rm -fr $LIBRARY_FOLDER/src
cp -r $TARGET_FOLDER/src $LIBRARY_FOLDER/src
rm -fr $TARGET_FOLDER
git checkout $LIBRARY_FOLDER/src/main/AndroidManifest.xml


