# Basic Makefile to build RPM and deb packages via fpm

BUILD_DIR = ovation-$(VERSION)
EPOCH = $(BUILD_NUMBER)

TARGET_DIR = $(BUILD_DIR)/opt/ovation
ARTIFACTS_DIR = target
DESKTOP_INFO_DIR = $(BUILD_DIR)/usr/share/applications
ICON_DIR = $(BUILD_DIR)/usr/share/icons/hicolor/48x48/apps

all: deb rpm

clean:
	rm *.deb
	rm *.rpm
	rm -rf $(BUILD_DIR)

stage:
	mkdir -p $(TARGET_DIR)
	mkdir -p $(DESKTOP_INFO_DIR)
	mkdir -p $(ICON_DIR)
	mkdir -p $(ARTIFACTS_DIR)

	cp ovation.desktop $(DESKTOP_INFO_DIR)
	cp ../application/src/main/linux/ovation_48x48.png $(ICON_DIR)/ovation.png
	cp -r ../application/target/ovation/* $(TARGET_DIR)

deb: stage
	DESCRIPTION="Ovation Desktop"
	fpm --force -s dir -t deb -n ovation -v $(VERSION) --epoch $(EPOCH) --license gpl3 --vendor Physion --category Science -d 'couchdb >= 1.4' -d 'java7-runtime' --url http://ovation.io -m 'Physion <support@ovation.io>' --description "$(DESCRIPTION)"  $(TARGET_DIR)=/opt $(ICON_DIR)=/usr/share/icons/hicolor/48x48 $(DESKTOP_INFO_DIR)=/usr/share
	mv *.deb $(ARTIFACTS_DIR)

rpm: stage
	DESCRIPTION="Ovation Desktop"
	fpm --force -s dir -t rpm -n ovation -v $(VERSION) --epoch $(EPOCH) --license gpl3 --vendor Physion --category Science -d 'couchdb >= 1.3' -d 'java >= 1.7' --url http://ovation.io -m 'Physion <support@ovation.io>' --description "$(DESCRIPTION)"  $(TARGET_DIR)=/opt $(ICON_DIR)=/usr/share/icons/hicolor/48x48 $(DESKTOP_INFO_DIR)=/usr/share
	mv *.rpm $(ARTIFACTS_DIR)
