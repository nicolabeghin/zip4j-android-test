# zip4j-android-test
Basic project to test [zip4j](https://github.com/srikanth-lingala/zip4j) library on Android

## Test status
[![CircleCI](https://circleci.com/gh/nicolabeghin/zip4j-android-test.svg?style=svg)](https://circleci.com/gh/nicolabeghin/zip4j-android-test)

## How to use (Docker)
Run tests on CircleCi-provided Android Docker images with 

`docker run -it circleci/android:api-27-node bash -c "cd ~ && git clone https://github.com/nicolabeghin/zip4j-android-test.git && cd zip4j-android-test && ./test_zip4j.sh"`

## How to use (Android Studio)

1. Clone this project repo
2. Download/update to latest zip4j library with `./update_zip4j.sh`
3. Open project in Android studio and run tests

![2020-03-04_22-57-45](https://user-images.githubusercontent.com/2743637/75927039-dec22b00-5e6b-11ea-8f4c-0db2460642dd.jpg)

## Expected results (TBD)

![2020-03-04_23-01-40](https://user-images.githubusercontent.com/2743637/75927170-25b02080-5e6c-11ea-80cc-e5e87dc1f3a0.jpg)
