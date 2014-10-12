#!/bin/sh

javaEXE=`/bin/readlink -f $(/usr/bin/which java)`
JAVA_HOME=`/usr/bin/dirname $(/usr/bin/dirname $javaEXE)`

echo "Please verify that Java is allowed to use ports under 1024!"
echo "You can use i.E. 'setcat cap_net_bind_service=+ep' '$javaEXE' to achieve this."
echo ""
echo "Don't forget to create your own ipxe.kpxe system image depending on your IP address using bin/create_ipxe_image.sh."
sleep 3

cd bin
$javaEXE -jar netbootd.jar
cd ..
