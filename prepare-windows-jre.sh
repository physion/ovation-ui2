#!/bin/sh

if [ -z "$1" ]; then
	#the unarchived jre-8u45-windows-i586.tar.gz would do
	echo "Usage $0 path-to-windows-jre";
	exit 1
fi;

WORK=jre-windows

if [ ! -e $WORK ]; then
	mkdir $WORK
fi;

cp -r $1/* $WORK
cd $WORK

if [ ! -e bin/java.exe ]; then
	echo "No java.exe found in $WORK. Argument probably wrong.";
fi;

pack200 -J-Xmx1024m lib/rt.jar.pack.gz lib/rt.jar
rm lib/rt.jar

zip -9 -r -y ../jre-windows.zip .
cd ..

if [ ! -e "unzipsfx.exe" ]; then
	wget ftp://ftp.info-zip.org/pub/infozip/win32/unz600xn.exe
	#the exe is a self-extracting archive, get just unzipsfx
	unzip unz600xn.exe unzipsfx.exe
fi;

cat unzipsfx.exe jre-windows.zip > jre-windows.exe

