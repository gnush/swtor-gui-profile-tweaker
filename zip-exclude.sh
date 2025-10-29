#!/bin/sh
#############################################
# Zips all files in SRC dir to TARGET       #
# Overwrites TARGET if it already exists    #
# File matching one of the EXCLUDE patterns #
# wont be packed into the archive.          #
#                                           #
# USAGE                                     #
# ./zip-exclude.sh TARGET SRC EXCLUDE..     #
#############################################

if [ -f "$1" ]; then
  rm "$1"
fi

if [ ! -d "$2" ]; then
  echo No dir specified
  exit 2
fi

TARGET="$1"
SRC="$2"
WD=$(pwd)

cd $SRC
zip -q -r $WD/$TARGET . --exclude ${@:3}
cd $WD