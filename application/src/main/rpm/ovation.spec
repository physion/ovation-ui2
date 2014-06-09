#
# spec file for package Ovation 
#
# Copyright  (c)  2014  Physion LLC
# This file and all modifications and additions to the pristine
# package are under the same license as the package itself.
#

%define _ovationroot	/usr/local/ovation
%define distver %(release="`rpm -q --queryformat='%{VERSION}' %{dist}-release 2> /dev/null | tr . : | sed s/://g`" ; if test $? != 0 ; then release="" ; fi ; echo "$release")


%define packer %(finger -lp `echo "$USER"` | head -n 1 | cut -d: -f 3)

Name:      ovation 
Summary:   Ovation Scientific Data Management System 
Version:   %_ovation_marketing_version
Release:   %_build_number%{?dist}
License:   GPLv3
Vendor:    Physion LLC
Group:     Applications/Database

                                                            
# Depencencies
# We need libuuid, but it's unavailable on CentOS 5; provided by e2fsprogs-libs
# Lib UUID is available in Amazon repo
Requires: java >= 1.7, couchdb >= 1.5.0

%description
Ovation Scientific Data Management System

For the most up-to-date information please
visit the Ovation web-site at http://ovation.io.

#list of files to be packaged into the rpm
%files
%defattr(755,root,root,755)
%_ovationroot

