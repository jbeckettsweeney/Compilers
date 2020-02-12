#!/bin/sh

java org.antlr.v4.Tool g.g4

javac Driver.java


java Driver < $1 > output.txt

