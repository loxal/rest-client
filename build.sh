#!/usr/bin/env bash

mvn clean assembly:assembly -D build.number=${build.number}
