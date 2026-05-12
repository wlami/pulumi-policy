PROJECT_NAME := Pulumi Policy Java SDK
include ../../build/common.mk

build::
	mvn -q -DskipTests package

test_fast::
	mvn -q test

test_all:: test_fast

install::
	mvn -q -DskipTests install

lint::
	@echo "No lint step configured for Java SDK."

clean:
	mvn -q clean
