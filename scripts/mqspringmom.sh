#!/bin/bash

export JAVA=$JAVA_HOME
export CONF_DIR=resources/

# libs
for i in lib/*.jar; do
  export CLASSPATH=$CLASSPATH:"$i"
done

APP_CLASSPATH="com.zimheral.mqspringmom.MqSpringMomApplication"
APP_NAME="${APP_CLASSPATH##*.}"
APP_START="$JAVA -Ddir=$PWD -Dfile.encoding=UTF-8 $APP_CLASSPATH --spring.config.location=$CONF_DIR"
PID_FILE=./PIDs/${APP_NAME,,}.pid

case $1 in
	start)
		if [ -r $PID_FILE ]; then
			echo "Valid PID found for ${APP_NAME} (`cat ${PID_FILE}`)"
			echo "If you already stopped the program, please delete the PID file before running again"
			exit 10
		fi

		echo -n "Starting ${APP_NAME}: "

		$APP_START >& logs/${APP_NAME,,}.log &

		PID_ID=$!
		EXIT_CODE_START=$?

		if [ $EXIT_CODE_START = 0 ]; then
			echo $PID_ID > $PID_FILE
			echo "PID set (PID: $PID_ID)"


			if [[ $2 == "-tail" || $3 == "-tail" ]]
			then
				clear;
				tail -0f logs/${APP_NAME,,}.log
			fi
		else
			echo "Error"
			exit 2
		fi

		exit 0
	;;
	
	stop)
		echo -n "Stopping ${APP_NAME}: "
		
		if [ -r $PID_FILE ]; then
			PID_ID=`cat $PID_FILE`
			kill -9 $PID_ID > /dev/null 2>&1
			rm -f $PID_FILE
		fi
		
		EXIT_CODE_KILL=$?
		if [ $EXIT_CODE_KILL = 0 ]; then
			echo "Ok"
			rm -f $PID_FILE
		else
			echo "Failed (PID not found $PID_ID)"
		fi

		if [[ $PID_ID == "" ]]
			then
			echo "No PID found, process stopped?"
			exit 0;
		fi
	;;
	

	*)
		echo "commands: ${APP_NAME,,}.sh start|stop"
		exit 1
	;;
esac
