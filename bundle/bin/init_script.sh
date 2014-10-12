#! /bin/sh
# /etc/init.d/netbootd
#
# Author: Jens Röwekamp <rowekamj@lsbu.ac.uk>
# Last modified: 23.08.2014

### BEGIN INIT INFO
# Provides:          netbootd
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Starts and stops the NetworkBoot Daemon
# Description:       Starts the front- and backend on it's ports.
### END INIT INFO

# Setup variables
#USER=root
USER=netbootd
INSTALLDIR=/opt/netbootd
PID=/var/run/netbootd.pid
LOG=/var/log/netbootd.log

# Constants
javaEXE=`/bin/readlink -f $(/usr/bin/which java)`
JAVA_HOME=`/usr/bin/dirname $(/usr/bin/dirname $javaEXE)`

do_start()
{
	if [ -f "$PID" ]; then
		echo "netbootd was started before"
		exit 2
	else
		/usr/bin/touch $PID
		/usr/bin/touch $LOG
		/bin/chown $USER:$USER $LOG
		/bin/chown $USER:$USER $PID
		/bin/su -l $USER -s /bin/sh -c "cd $INSTALLDIR/bin; $javaEXE -jar netbootd.jar 2>$LOG & echo \$! > $PID"
	fi
	}

do_stop()
{
	if [ -f "$PID" ]; then
		kill `cat $PID`
		rm $PID
	else
		echo "netbootd was not running"
		exit 2
	fi
}
	
case "$1" in
    start)
		do_start
		;;
    stop)
       	do_stop
        ;;
    restart)
        if [ -f "$PID" ]; then
            do_stop
            do_start
        else
            echo "service not running, will do nothing"
            exit 1
        fi
        ;;
	status)
		if [ -f "$PID" ]; then
			echo "netbootd is running"
		else
			echo "netbootd is stopped"
		fi
		;;
    *)
        echo "usage: netbootd {start|stop|restart|status}" >&2
        exit 3
        ;;
esac

exit 0
