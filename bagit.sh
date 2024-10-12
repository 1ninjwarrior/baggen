#!/bin/bash

# Compile all Java files
javac *.java 

# Loop through files g1.txt to g15.txt
for i in {1..15}
do
    echo "Processing g${i}.txt"
    java BagIt "testfiles/g${i}.txt"
done

# Clean up compiled class files
rm -f *.class
