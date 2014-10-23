#!/bin/sh

conf="bank.json"

if [ ! -z "$1" ]; then
  conf="$1"
fi

vertx run bank.js -conf "${conf}" -cluster
