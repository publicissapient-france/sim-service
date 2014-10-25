#!/bin/sh

conf=store.json
if [ ! -z $1 ]
then
  conf=$1
fi

vertx run src/main/java/fr/xebia/vertx/store/StoreVerticle.java -conf $conf -cluster