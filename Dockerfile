FROM quay.io/keycloak/keycloak:20.0.0 as build

ARG RELEASE
ARG GITHUB_TOKEN

ENV KC_HEALTH_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB=mariadb
ENV KC_HTTP_ENABLED=true
ENV KC_HTTP_RELATIVE_PATH=/auth
ENV KC_PROXY=edge

# Install custom providers
ADD --chown=keycloak https://_:${GITHUB_TOKEN}@maven.pkg.github.com/ChameleonCloud/keycloak-chameleon/org/chameleoncloud/keycloak-chameleon/${RELEASE}/keycloak-chameleon-${RELEASE}-jar-with-dependencies.jar /opt/keycloak/providers
# Install MariaDB driver
ADD --chown=keycloak https://dlm.mariadb.com/2531428/Connectors/java/connector-java-3.0.8/mariadb-java-client-3.0.8.jar /usr/share/java
RUN /opt/keycloak/bin/kc.sh build \
    --spi-chameleon-extended-api-enabled=true \
    --spi-chameleon-post-logout-page-enabled=true \
    --spi-chameleon-terms-and-conditions-page-enabled=true

FROM quay.io/keycloak/keycloak:20.0.0 as release

COPY --from=build /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak

ENV KC_HTTP_ENABLED=true
ENV KC_HTTP_RELATIVE_PATH=/auth
ENV KC_PROXY=edge
ENV KC_DB=mariadb

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]