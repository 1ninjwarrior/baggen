# Variables
JAVAC = javac
JAVA = java
MAIN_CLASS = BagIt
SOURCES = $(wildcard *.java)
CLASSES = $(SOURCES:.java=.class)

# Default target
all: $(CLASSES)

# Compile Java files
%.class: %.java
	$(JAVAC) $<

# Run the application
run: all
	$(JAVA) $(MAIN_CLASS)

# Clean up class files
clean:
	rm -f *.class