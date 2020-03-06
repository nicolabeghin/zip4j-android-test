#!/bin/bash

./update_zip4j.sh
./gradlew :zip4j:testReleaseUnitTest --tests net.lingala.zip4j.*