echo "Shutting down Calc Tomcat" 
if [ ! $JRE_HOME ]; then
	export JRE_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
	echo "JRE_HOME set to $JRE_HOME" 
else 
	echo "Using JRE_HOME $JRE_HOME" 
fi

cd install_directory/calc-server/tomcat/bin
./shutdown.sh
