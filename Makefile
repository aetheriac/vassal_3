LIBDIR=../VASSAL-lib
LIBS=

CLASSPATH=$(shell find $(LIBDIR) -name '*.jar' | tr '\n' ':').

JAVAPATH=/usr/java/jdk1.6.0

JC=$(JAVAPATH)/bin/javac
JFLAGS=-classpath $(CLASSPATH)
JCFLAGS=-d classes -source 1.4 -Xlint -Xlint:-serial -Xlint:-path
JRFLAGS=

JAR=$(JAVAPATH)/bin/jar
JDOC=$(JAVAPATH)/bin/javadoc

SRC=$(shell find -name '*.java' | sed 's/^\.\///')
CLASSES=$(SRC:.java=.class)

vpath %.class $(shell find classes -type d)

%.class: %.java
	$(JC) $(JCFLAGS) $(JFLAGS) $<

all: $(CLASSES)

jar:
	$(JAR) cvf Vengine.jar images/* -C classes VASSAL 

doc:
	$(JDOC) -d doc $(SRC)

.PHONY: clean

clean:
	$(RM) -r classes/*
