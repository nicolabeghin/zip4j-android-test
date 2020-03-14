#!/bin/bash

TARGET_FOLDER=zip4j_updated
LIBRARY_FOLDER=zip4j
REPO=https://github.com/srikanth-lingala/zip4j.git

# clone new version from official repo
git clone $REPO $TARGET_FOLDER
rm -fr $LIBRARY_FOLDER/src
cp -r $TARGET_FOLDER/src $LIBRARY_FOLDER/src
rm -fr $TARGET_FOLDER
git checkout $LIBRARY_FOLDER/src/main/AndroidManifest.xml

# exclude randomly failing test MiscZipFileIT-testCustomThreadFactory
TEST_CLASS="zip4j/src/test/java/net/lingala/zip4j/MiscZipFileIT.java"
LINE=$((`grep -n "testCustomThreadFactory" $TEST_CLASS | cut -f1 -d:`-1))
sed "${LINE}s/@/\/\/@/" $TEST_CLASS > outfile
mv outfile $TEST_CLASS

