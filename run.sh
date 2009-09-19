#!/bin/bash

# PROJ_HOME=/home/bassani/Desktop/ProjetosDiversos/Htrack2/Codigos/jhtrack2
PROJ_HOME=`pwd`

export LD_LIBRARY_PATH=$PROJ_HOME/LIBs/jogl/1.1.1.a/linux-i586:$PROJ_HOME/LIBs/wiiusej/0.12b/linux-i386:$PROJ_HOME/LIBs/wiiusej/0.12b/linux-i386_extra:/usr/lib/

java -Djava.library.path=$PROJ_HOME/LIBs/jogl/1.1.1.a/linux-i586:$PROJ_HOME/LIBs/wiiusej/0.12b/linux-i386:$PROJ_HOME/LIBs/wiiusej/0.12b/linux-i386_extra -Dfile.encoding=UTF-8 -classpath $PROJ_HOME/bin:$PROJ_HOME/LIBs/jogl/1.1.1.a/linux-i586/jogl.jar:$PROJ_HOME/LIBs/jogl/1.1.1.a/linux-i586/gluegen-rt.jar:$PROJ_HOME/LIBs/wiiusej/0.12b/wiiusej.jar JHTrack2
