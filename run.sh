#!/usr/bin/env bash

./mvnw clean validate assembly:assembly
java -jar target/Epvin-*-with-dependencies.jar