#!/bin/bash
mkdir ./bin
javac -cp "./src:./lib/jfreechart-1.5.3.jar:./lib/org.jfree.svg-4.2.jar" -encoding UTF-8 -d ./bin ./src/application/Galaxy_SP2022.java

