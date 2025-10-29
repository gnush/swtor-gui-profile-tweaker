# Depends on: rsync

shell := /bin/bash

.PHONY: pack

all: linux windows mac
	@rm -rf $(TMP)

linux: pack tmp
	@echo Make linux package
	@mkdir -p $(TMP)/linux/bin
	@mkdir -p $(TMP)/linux/lib
	cp target/pack/bin/guiStateTweaker $(TMP)/linux/bin
	find target/pack/lib -type f\
		-not -iname '*win.jar'\
		-not -iname '*mac.jar'\
		-exec cp {} '$(TMP)/linux/lib' ';'
	sh zipall.sh linux.zip $(TMP)/linux

windows: pack tmp
	@echo Make windows package
	@mkdir -p $(TMP)/windows/bin
	@mkdir -p $(TMP)/windows/lib
	cp target/pack/bin/guiStateTweaker.bat $(TMP)/windows/bin
	find target/pack/lib -type f\
		-not -iname '*linux.jar'\
		-not -iname '*mac.jar'\
		-exec cp {} '$(TMP)/windows/lib' ';'
	sh zipall.sh windows.zip $(TMP)/windows

mac: pack tmp
	@echo Make mac package
	@mkdir -p $(TMP)/mac/bin
	@mkdir -p $(TMP)/mac/lib
	cp target/pack/bin/guiStateTweaker $(TMP)/mac/bin
	find target/pack/lib -type f\
		-not -iname '*linux.jar'\
		-not -iname '*win.jar'\
		-exec cp {} '$(TMP)/mac/lib' ';'
	sh zipall.sh mac.zip $(TMP)/mac

pack:
	sbt pack

tmp:
	$(eval TMP := $(shell mktemp -d))