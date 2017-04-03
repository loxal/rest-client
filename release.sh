#!/usr/bin/env bash

./mvnw clean assembly:assembly -D build.number=${build.number}
cp target/Epvin-v*-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin
cp target/Epvin-v*-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin/Epvin.jar

cp target/Epvin-v*-jar-with-dependencies.jar /Applications/Epvin.jar
