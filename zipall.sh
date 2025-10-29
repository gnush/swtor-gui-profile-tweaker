#!/bin/sh
##########################################
# Zips all files in DIR dir to TARGET    #
# Deletes the original files if done     #
# Overwrites TARGET if it already exists #
#                                        #
# USAGE                                  #
# ./zipall.sh TARGET DIR                #
##########################################

if [ -f "$1" ]; then
  rm "$1"
fi

if [ ! -d "$2" ]; then
  echo No dir specified
  exit 2
fi

TARGET="$1"
DIR="$2"
WD=$(pwd)

cd $DIR
zip -q -r $WD/$TARGET .
cd $WD