#!/bin/bash 

cd `dirname $0`
bin_path=`pwd`
base=$bin_path/..
wagon_conf=$base/conf/wagon.properties
logback_configurationFile=$base/conf/logback.xml

if [ -f $base/bin/wagon.pid ] ; then
	echo "found wagon.pid , Please run stop.sh first ,then startup.sh" 2>&2
    exit 1
fi

if [ ! -d $base/logs/wagon ] ; then 
	mkdir -p $base/logs/wagon
fi

## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi

if [ -z "$JAVA" ]; then
  	echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.7) in your PATH." 2>&2
    exit 1
fi

case "$#" 
in
0 ) 
	;;
1 )	
	var=$*
	if [ -f $var ] ; then 
		wagon_conf=$var
	else
		echo "THE PARAMETER IS NOT CORRECT.PLEASE CHECK AGAIN."
        exit
	fi;;
2 )	
	var=$1
	if [ -f $var ] ; then
		wagon_conf=$var
	else 
		if [ "$1" = "debug" ]; then
			DEBUG_PORT=$2
			DEBUG_SUSPEND="n"
			JAVA_DEBUG_OPT="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
		fi
     fi;;
* )
	echo "THE PARAMETERS MUST BE TWO OR LESS.PLEASE CHECK AGAIN."
	exit;;
esac

str=`file $JAVA_HOME/bin/java | grep 64-bit`
if [ -n "$str" ]; then
	JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
	if [[ "$JAVA_VERSION" > "1.7" ]]; then
		JAVA_OPTS="-server -Xms128m -Xmx128m -Xss256k -XX:SurvivorRatio=2 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"
	else
		JAVA_OPTS="-server -Xms128m -Xmx128m -XX:SurvivorRatio=2 -Xss256k -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError"
	fi
else
	JAVA_OPTS="-server -Xms128m -Xmx128m"
fi

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"
wagon_OPTS="-Dlogback.configurationFile=$logback_configurationFile -Dwagon.conf=$wagon_conf -DappName=wagon_executor"

if [ -e $wagon_conf -a -e $logback_configurationFile ]
then

    	MAIN_CLASS="com.youzan.wagon.console.WagonConsoleLauncher"
    	for i in $base/lib/*;
    		do CLASSPATH=$i:"$CLASSPATH";
    	done
    	CLASSPATH="$base/conf:$CLASSPATH";

        echo "cd to $bin_path for workaround relative path"
        cd $bin_path

        echo LOG CONFIGURATION : $logback_configurationFile
        echo conf : $wagon_conf
        echo CLASSPATH :$CLASSPATH
        $JAVA $JAVA_OPTS $JAVA_DEBUG_OPT $wagon_OPTS -classpath $CLASSPATH $MAIN_CLASS 1>>$base/logs/wagon/wagon.log 2>&1 &
        echo $! > $base/bin/wagon.pid

        echo "cd to $bin_path for continue"
        cd $bin_path
else
        echo " conf("$wagon_conf") OR log configration file($logback_configurationFile) is not exist,please create then first!"
fi