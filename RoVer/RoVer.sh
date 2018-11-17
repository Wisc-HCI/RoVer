#!/bin/bash

javac -cp "./bin:../prism-4.3.1-src/lib/colt.jar:../prism-4.3.1-src/lib/jhoafparser.jar:../prism-4.3.1-src/lib/pepa.zip:../prism-4.3.1-src/lib/prism.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/jre/lib/ext/jfxrt.jar" -d bin $(find ./src/* | grep .java)

export DYLD_LIBRARY_PATH=$DYLD_LIBRARY_PATH:../prism-4.3.1-src/lib/
# LD_LIBRARY_PATH if linux

/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java -Djava.library.path=../prism-4.3.1-src/lib/ -Dfile.encoding=UTF-8 -classpath "./bin:../prism-4.3.1-src/lib/colt.jar:../prism-4.3.1-src/lib/jhoafparser.jar:../prism-4.3.1-src/lib/pepa.zip:../prism-4.3.1-src/lib/prism.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/jre/lib/ext/jfxrt.jar" controller.Main

