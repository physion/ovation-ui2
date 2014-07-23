#!/bin/bash

VERSION=$1

#git checkout v$VERSION

BUILD_DIR=ovation-$VERSION

TARGET_DIR=$BUILD_DIR/opt/ovation
DESKTOP_INFO_DIR=$BUILD_DIR/usr/share/applications
ICON_DIR=$BUILD_DIR/usr/share/icons/hicolor/48x48/apps

rm -rf $BUILD_DIR
#rm ovation_$VERSION*.xz

mkdir -p $TARGET_DIR
mkdir -p $DESKTOP_INFO_DIR
mkdir -p $ICON_DIR

cp ovation.desktop $DESKTOP_INFO_DIR
cp ../application/src/main/linux/ovation_48x48.png $ICON_DIR/ovation.png
cp -r ../application/target/ovation/* $TARGET_DIR

# Build the package
fpm --force -s dir -t deb -n ovation -v $VERSION --license gpl3 --vendor Physion --category Science -d 'couchdb >= 1.4' -d 'java7-runtime' --url http://ovation.io -m 'Barry Wark <support@ovation.io>' $TARGET_DIR=/opt $ICON_DIR=/usr/share/icons/hicolor/48x48 $DESKTOP_INFO_DIR=/usr/share

lintian ovation_$VERSION*.deb
