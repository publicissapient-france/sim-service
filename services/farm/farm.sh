#!/bin/sh

conf=config.json
if [ ! -z $1 ]
then
  conf=$1
fi

vertx run src/main/java/Farm.java -conf $conf -cluster
