#!/bin/bash

javac *.java 

java BagIt "$@"

rm -f *.class
