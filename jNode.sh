#!/usr/bin/env bash
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILENAME="jnode-platform-0.0.1-SNAPSHOT.jar"
OS=$(uname -s)

if [  -f $CWD"/"$JAR_FILENAME ] ; then
    JNODE_JAR=$CWD"/"$JAR_FILENAME
fi

if [ -z "$JNODE_JAR" ] ; then
    JNODE_JAR=$CWD"/target/jNode/"$JAR_FILENAME
fi

if [ ! -f "$JNODE_JAR" ] ; then
    echo "jnode jar is missing"
    exit 1
fi

if [ "$OS" == "Linux" ]; then
	rmem_max=`sysctl -b net.core.rmem_max`
	if [ $rmem_max -lt 5242880 ]; then
		echo "Need to extend maximum socket read buffer"
		sudo sysctl -w net.core.rmem_max=5242880
	fi

	wmem_max=`sysctl -b net.core.wmem_max`
	if [ $wmem_max -lt 5242880 ]; then
		echo "Need to extend maximum socket write buffer"
		sudo sysctl -w net.core.wmem_max=5242880
	fi
fi

#if [ "$OS" == "Darwin" ] || [[ "$OS" == "Windows"* ]] || [[ "$OS" == "MINGW"* ]]; then
    java -jar -Djava.net.preferIPv4Stack=true $JNODE_JAR $@
#else
#    echo "$OS"
#    java -jar $JNODE_JAR $@
#fi
