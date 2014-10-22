#!/bin/bash

. env.sh

rsync -azv -e "ssh -i $KEY" ../services $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR