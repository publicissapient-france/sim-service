#!/bin/sh

conf=factory.json
if [ ! -z $1 ]
then
  conf=$1
fi

vertx run src/main/java/fr/xebia/vertx/factory/FactoryVerticle.java -conf $conf -cluster
