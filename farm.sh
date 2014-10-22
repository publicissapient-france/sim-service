#!/bin/sh

conf=config.json
if [ ! -z $1 ]
then
  conf=$1
fi

vertx run services/farm/src/main/java/Farm.java -conf services/farm/$conf -cluster 
