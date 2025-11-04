.PHONY: pack

all: linux windows mac

# TODO: add direct start script to archives or move start script from bin to . (and refactor paths?)

linux: pack
	gcc -o target/pack/guiStateTweaker launcher/linux.c
	sh zip-exclude.sh linux.zip target/pack *win.jar *mac.jar Makefile VERSION bin/* *.bat *.exe

windows: pack
	x86_64-w64-mingw32-gcc -o target/pack/guiStateTweaker.exe launcher/windows.c
	sh zip-exclude.sh windows.zip target/pack *linux.jar *mac.jar Makefile VERSION bin/guiStateTweaker guiStateTweaker

mac: pack
	sh zip-exclude.sh mac.zip target/pack *linux.jar *win.jar Makefile VERSION *.bat *.exe guiStateTweaker

pack:
	sbt pack