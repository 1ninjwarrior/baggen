#!/bin/bash

# Compile all Java files in the current directory, placing class files in the correct package directory
javac -d . *.java

# Run the BagIt class with the package name
java -cp . files.BagIt "$@"

# Clean up class files
rm -f files/*.class