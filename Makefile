.PHONY: build
build:
	mvn clean install

.PHONY: publish
publish:
	mvn deploy -Dregistry=https://maven.pkg.github.com/ChameleonCloud

target/keycloak-chameleon.jar: build
	mvn -B package

deploy-dev: target/keycloak-chameleon.jar
	scp target/keycloak-chameleon.jar admin02.uc.chameleoncloud.org:~/
