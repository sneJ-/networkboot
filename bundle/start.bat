echo off
echo.
echo Don't forget to create your own ipxe.kpxe system image depending on your IP address using bin/create_ipxe_image.sh.
echo.
timeout /T 3 > NUL

cd bin
java -jar netbootd.jar
cd ..

echo on