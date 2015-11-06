#!/usr/bin/env bash

mvn clean validate assembly:assembly
java -jar target/Epvin-*-with-dependencies.jar