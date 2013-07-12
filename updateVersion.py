#!/usr/bin/python

from tempfile import mkstemp
from shutil import move
from os import remove, close, walk
import re
import sys

def replace(file_path, pattern, subst):
    #Create temp file
    fh, abs_path = mkstemp()
    new_file = open(abs_path,'w')
    old_file = open(file_path)

    found_version = False
    try:        
       for line in old_file:
           if re.match(pattern, line):
               found_version = True
           new_file.write(re.sub(pattern, subst, line))
       if not found_version:
           new_file.write(subst)
    finally:
        #close temp file
        new_file.close()
        close(fh)
        old_file.close()
    
    #Remove original file
    remove(file_path)
    #Move new file
    move(abs_path, file_path)


def updateVersion(file_path, version):
    pattern = "OpenIDE-Module-Specification-Version: .*"
    sub = "OpenIDE-Module-Specification-Version: " + version
    replace(file_path, pattern, sub)

def updateManifestsInTree(rootPath, version):
    
    for root, dirs, files in walk(rootPath):
        for f in files:
            if (f == 'manifest.mf'):
                full_path = root + '/' + f
                print('Updating ' + full_path + ' to version ' + version + '...')
                updateVersion(full_path, version)

if __name__ == '__main__':
    version = sys.argv[1]
    updateManifestsInTree('.', version)
