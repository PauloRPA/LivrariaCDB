#!/bin/bash

javac -cp src/ -d 'target/' src/**/*.java
java -cp 'target/' app.Application
