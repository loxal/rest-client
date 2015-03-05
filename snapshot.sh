#!/usr/bin/env bash

mvn clean assembly:assembly
cp target/Epvin-v*-SNAPSHOT-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin
cp target/Epvin-v*-SNAPSHOT-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin/Epvin-SNAPSHOT.jar
