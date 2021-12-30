#!/bin/sh
find src -type f -name *.class -exec rm -f {} \;
rm -rf target
rm -rf lib
