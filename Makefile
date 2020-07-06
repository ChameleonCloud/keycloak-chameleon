.PHONY: build
build:
	mvn clean install

.PHONY: publish
publish:
	mvn deploy -Dregistry=https://maven.pkg.github.com/ChameleonCloud
