#!/bin/bash
cd ./src
javadoc -encoding UTF-8 -sourcepath . -cp ".:../lib/jfreechart-1.5.3.jar:../lib/org.jfree.svg-4.2.jar" -d ../doc/javadoc -version -author *
cd ..

