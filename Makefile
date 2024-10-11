all: BaggingProblem.class

BaggingProblem.class: BaggingProblem.java
    javac BaggingProblem.java

clean:
    rm -f *.class