FROM quay.io/keycloak/keycloak:17.0.0 as build

ARG RELEASE
ARG GITHUB_TOKEN

ENV KC_HEALTH_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB_VENDOR=mysql
# Install custom providers
ADD --chown=keycloak https://_:${GITHUB_TOKEN}@maven.pkg.github.com/ChameleonCloud/keycloak-chameleon/org/chameleoncloud/keycloak-chameleon/${RELEASE}/keycloak-chameleon-${RELEASE}-jar-with-dependencies.jar /opt/keycloak/providers
RUN /opt/keycloak/bin/kc.sh build --spi-keycloak-server-keycloak-chameleon-enabled=true

FROM quay.io/keycloak/keycloak:17.0.0 as release

COPY --from=build /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]