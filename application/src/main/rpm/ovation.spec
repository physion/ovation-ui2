#
# spec file for package Ovation 
#
# Copyright  (c)  2014  Physion LLC
# This file and all modifications and additions to the pristine
# package are under the same license as the package itself.
#

%define _ovationroot	/opt/ovation
%define distver %(release="`rpm -q --queryformat='%{VERSION}' %{dist}-release 2> /dev/null | tr . : | sed s/://g`" ; if test $? != 0 ; then release="" ; fi ; echo "$release")
%define packer %(finger -lp `echo "$USER"` | head -n 1 | cut -d: -f 3)
%define name ovation
%define buildroot %{_topdir}/${name}-${%_ovation_version}-root

Name:      %{name}
Summary:   Ovation Scientific Data Management System 
Version:   %_ovation_version
Release:   %_build_number%{?dist}
License:   GPLv3
Vendor:    Physion LLC
Source:    https://github.com/physion/ovation-ui2
URL:       http://ovation.io
Group:     Applications/Database
BuildRoot: %{buildroot}

                                                            
# Depencencies
Requires: java >= 1.7, couchdb >= 1.5.0

%description
Ovation Scientific Data Management System

For the most up-to-date information please
visit the Ovation web-site at http://ovation.io.

%build
cp -r ovation %{_ovationroot}
# Copy .desktop file: https://developer.gnome.org/integration-guide/stable/desktop-files.html.en
# Copy icon: https://developer.gnome.org/integration-guide/stable/icons.html.en


#list of files to be packaged into the rpm
%files
%defattr(755,root,root,755)
%_ovationroot
/usr/share/icons/hicolor/48x48/apps/ovation.png
/usr/share/applications/ovation.desktop

