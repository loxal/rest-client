#!/usr/bin/env bash

mvn clean assembly:assembly -Dbuild.number=${build.number}
cp target/Epvin-v*-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin
cp target/Epvin-v*-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin/Epvin-SNAPSHOT.jar
