#!/bin/sh

username="todo"
hostname="todo"

# DO NOT configure variables after this comment
script="${0}"
action="${1?'action as argument #1 must be provided'}"
module="${2?'module as argument #2 must be provided'}"
config="${3:-${module}}" # defaults to module's name

dirname="$(dirname ${script})";
runfile="${dirname}/${module}/$(cat ${dirname}/${module}/${module}.main)"
cfgfile="${dirname}/${module}/${config}.json"
pidfile="${dirname}/${module}/.${config}.pid"
logfile="${dirname}/${module}/.${config}.log"

running() {
    test -f "${pidfile}" && kill -0 $(cat "${pidfile}")
}

start() {
    vertx run "${runfile}" -conf "${cfgfile}" -cluster &> "${logfile}" &
    echo "${!}" > "${pidfile}"
}

stop() {
    kill "$(cat "${pidfile}")" && rm "${pidfile}"
}

logs() {
    tail -10f "${logfile}"
}

case "${action}" in
    remote-* )
        rsync -az "${dirname}" "${username}@${hostname}:services" && ssh "${username}@${hostname}" "services/services.sh ${action#remote-} ${module} ${config}"
        exit "${?}" ;; # if action is remotely-executed local part of the script must stop here
    start )
        running && echo "${config} is still running" || start ;;
    stop )
        running && stop || echo "${config} is not running" ;;
    restart )
        running && (stop && start) || start ;;
    status )
        running && echo "${config} is started" || echo "${config} is stopped" ;;
    logs )
        logs ;;
    * )
        echo "${script} '[remote-]start|status|logs|stop|restart' module [config]" && exit 1 ;;
esac