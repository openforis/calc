echo "Starting Collect Tomcat" 
if [ ! $JRE_HOME ]; then
	export JRE_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
	echo "JRE_HOME set to $JRE_HOME" 
else 
	echo "Using JRE_HOME $JRE_HOME" 
fi

if [ ! $JRE_HOME ]; then
	echo Error: cannot determine JRE_HOME path automatically
	echo Please make sure you have Java Runtime Environment installed
	echo and define JRE_HOME environment variable
else 
	cd install_directory/calc-server/tomcat/bin
	./startup.sh
fi
