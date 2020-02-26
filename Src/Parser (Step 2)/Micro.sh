#!/bin/sh

java org.antlr.v4.Tool Gram.g4

javac Gram*.java

javac Driver.java

java Driver < $1 
