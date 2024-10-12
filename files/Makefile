# Variables
JAVAC = javac
JAVA = java
MAIN_CLASS = files.BagIt
SOURCES = $(wildcard *.java)
CLASSES = $(SOURCES:.java=.class)

# Default target
all: $(CLASSES)

# Compile all Java files
$(CLASSES): $(SOURCES)
	$(JAVAC) $(SOURCES)

# Run the application
run: all
	$(JAVA) -cp . $(MAIN_CLASS)

# Clean up class files
clean:
	rm -f *.class