[ ! -d bin ] && mkdir -p bin ; cd ./src && javac sqlbench.java -d ../bin && cd .. && jar -cvfm sqlbench.jar manifest.txt -C bin .
