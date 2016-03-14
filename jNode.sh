#!/usr/bin/env bash
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILENAME="jnode-platform-0.0.1-SNAPSHOT.jar"

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

OS=$(uname -s)

if [ "$OS" == "Darwin" ]; then
    java -jar -Djava.net.preferIPv4Stack=true $JNODE_JAR $@
else
    java -jar $JNODE_JAR $@
fi
