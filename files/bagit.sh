#!/bin/bash

rm -f *.class

javac *.java

java -cp .. files.BagIt "$1"