#!/usr/bin/env bash

mvn clean assembly:assembly
java -jar target/Epvin-*-with-dependencies.jar