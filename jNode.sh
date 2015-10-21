
if [ -z "$JNODE_JAR" ] ; then
    CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    JNODE_JAR=$CWD"/target/jnode-0.0.1-SNAPSHOT.jar"
fi

if [ ! -f "$JNODE_JAR" ] ; then
    echo "jnode jar is missing"
    exit 1
fi

java -jar $JNODE_JAR $@
