#!/bin/sh

# this variable must be configured to point to your remote server, e.g. :
# remote_server="user@myserver.com"
# remote_server="user@8.8.8.8"
# remote_server="alias" if 'alias' is defined in your ~/.ssh/config file
remote_server="sim-factory"

# DO NOT configure this script after this comment
script="${0}"
action="${1}"
module="${2}"
config="${3:-${module}}" # defaults to module's name
dirname="$(dirname ${script})"

usage() {
    echo "${script} '[local-]start|stop|status|restart|list|logs' module [config]" && exit 1
}

running() {
    test -f "${pidfile}" && kill -0 $(cat "${pidfile}") > /dev/null 2>&1
}

start() {
    nohup vertx run "${runfile}" -conf "${cfgfile}" -cluster -cluster-host 0.0.0.0 > "${logfile}" 2>&1 & echo "${!}" > "${pidfile}"
}

stop() {
    kill "$(cat "${pidfile}")" && rm "${pidfile}"
}

if [ $# -lt 2 ]; then usage; fi

case "${action}" in

    start | stop | status | restart | list | logs )

        # synchronize files from script directory
        rsync -az "${dirname}" "${remote_server}:/opt" && ssh "${remote_server}" "bash -l -c \"/opt/services.sh local-${action} ${module} ${config}\"" ;;

    local-start | local-stop | local-status | local-restart | local-list | local-logs )

        # go to module directory, due to vert.x modules' path concerns
        cd "${dirname}/${module}"
        runfile="$(cat ${module}.main)"
        cfgfile="${config}.json"
        pidfile=".${config}.pid"
        logfile=".${config}.log"
        trace="$(whoami)@$(hostname)"

        case "${action#local-}" in
            start )
                running && echo "${trace}: ${config} is still running" || start ;;
            stop )
                running && stop || echo "${trace}: ${config} is not running" ;;
            status )
                running && echo "${trace}: ${config} is started" || echo "${trace}: ${config} is stopped" ;;
            restart )
                running && (stop && start) || start ;;
            list )
                ls -1a .*.pid | sed "s/\.pid//" | sed "s/\.//" || echo "No ${module} instance running" ;;
            logs )
                echo "${trace}: ${logfile}" && tail -10f "${logfile}" ;;
        esac ;;

    * )
        usage ;;

esac
