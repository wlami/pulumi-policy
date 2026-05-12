PROJECT_NAME := policy
SUB_PROJECTS := sdk/nodejs/policy sdk/python sdk/java
include build/common.mk

.PHONY: ensure
ensure::
	# Golang dependencies for the integration tests.
	cd ./tests/integration && go mod download && go mod tidy

.PHONY: build_java test_java install_java clean_java
build_java: sdk/java_build
test_java: sdk/java_test_fast
install_java: sdk/java_install
clean_java:
	$(MAKE) -C sdk/java clean

.PHONY: publish_packages
publish_packages:
	$(call STEP_MESSAGE)
	./scripts/publish_packages.sh

.PHONY: test_all
test_all::
	cd ./tests/integration && go test . -v -timeout 30m

