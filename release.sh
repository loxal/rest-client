#!/usr/bin/env bash

mvn clean assembly:assembly
cp target/Epvin-v*-RELEASE-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin
cp target/Epvin-v*-RELEASE-jar-with-dependencies.jar ~/Google\ Drive/public/Epvin/Epvin.jar