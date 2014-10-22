#!/bin/bash

. env.sh

rsync -azv -e "ssh -i $KEY" --include="services/" --include="services/**" --include="init.d/" --include="init.d/*" --exclude="*" ../ $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR