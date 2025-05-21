ifneq (,$(wildcard ./.env))
	include .env
	export
	ENV_FILE_PARAM = --env-file .env
endif

.PHONY: build
build:
	mvn clean install

.PHONY: publish
publish:
	mvn deploy -Dregistry=https://maven.pkg.github.com/ChameleonCloud

.PHONY: docker-build
docker-build:
	docker build . -t keycloak-chameleon --build-arg GITHUB_TOKEN="${GITHUB_TOKEN}"
