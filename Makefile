.PHONY: pack

all: linux windows mac

linux: pack
	sh zip-exclude.sh linux.zip target/pack *win.jar *mac.jar Makefile VERSION *.bat

windows: pack
	sh zip-exclude.sh windows.zip target/pack *linux.jar *mac.jar Makefile VERSION bin/guiStateTweaker

mac: pack
	sh zip-exclude.sh mac.zip target/pack *linux.jar *win.jar Makefile VERSION *.bat

pack:
	sbt pack