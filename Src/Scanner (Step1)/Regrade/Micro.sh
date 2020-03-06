#!/bin/sh
java org.antlr.v4.Tool g.g4
javac g*.java
javac Driver.java
java Driver < $1
