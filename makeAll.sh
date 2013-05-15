#!/bin/sh

pushd ../ovation-2.0/
mvn clean source:jar install -DskipTests
popd
mvn clean install -DskipTests