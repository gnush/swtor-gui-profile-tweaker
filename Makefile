.PHONY: pack

all: linux windows mac

linux: pack
	gcc -o target/pack/guiStateTweaker launcher/linux.c
	bash zip-exclude.sh linux.zip target/pack *win.jar *mac.jar Makefile VERSION bin/* *.bat *.exe
	rm target/pack/guiStateTweaker

windows: pack
	x86_64-w64-mingw32-gcc -o target/pack/guiStateTweaker.exe launcher/windows.c
	bash zip-exclude.sh windows.zip target/pack *linux.jar *mac.jar Makefile VERSION bin/*
	rm target/pack/guiStateTweaker.exe

# github runner fails with mingw build with:
# 	/usr/bin/x86_64-w64-mingw32-ld: /tmp/cc91sLmj.o:windows.c:(.text+0x63): undefined reference to `_get_pgmptr'
# look into why this is the case, meanwhile use prebuild binary instead
windows-bin: pack
	cp launcher/windows.exe target/pack/guiStateTweaker.exe
	bash zip-exclude.sh windows.zip target/pack *linux.jar *mac.jar Makefile VERSION bin/*
	rm target/pack/guiStateTweaker.exe

mac: pack
	bash zip-exclude.sh mac.zip target/pack *linux.jar *win.jar Makefile VERSION *.bat *.exe

pack:
	sbt pack