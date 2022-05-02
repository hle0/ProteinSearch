#!/bin/bash
# You don't need to grade this (see README.md)
rm -rf build
mkdir build
cd src
javac -d ../build *.java
cd ../build
jar --create --file ../release.jar --main-class ProteinSearch .
cd ..