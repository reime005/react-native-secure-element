#!/bin/sh

./gradlew clean bundleGeneralReleaseAar publish bintrayUpload
