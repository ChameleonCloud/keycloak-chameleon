# Chameleon Keycloak extension

This single artifact contains a few extensions to Keycloak:

* An OpenID protocol mapper, which
  returns a list of the user's linked federated identity provider (aliases) on
  a new user-defined claim name. In order for this to be useful with multiple
  providers, you must configure it as a multi-value claim.
* An OpenID protocol mapper, which returns a list of "projects" (groups) with
  additional custom properties set (e.g., "nickname".)
* A Keycloak theme that adds Chameleon branding and hides
  the username/password login flow. The theme also contains the terms and
  conditions for use of the Chameleon platform.
* A required action that requires a user to update their profile
  and set required fields that are non-standard.

## Build

```shell
make build
```

### Publishing new versions

This package is currently published to GitHub Packages.

```shell
make publish
```

### Testing a version

Keycloak providers (plugins, extensions, etc.) must be placed in the in the Keycloak modules directory 
(/opt/keycloak/providers when using the 
[Keycloak Docker image](https://quay.io/repository/keycloak/keycloak?tab=info). 
Keycloak must be restarted (and rebuilt if auto-build is not enabled) to load the provider.

> **Note**: if a theme template is updated, hot-reloading doesn't seem to pick up template changes. In this case, a restart of the Keycloak service is necessary.

```shell
version="<version>"
github_package="<JAR asset URL from https://github.com/ChameleonCloud/keycloak-chameleon/packages/304507>"
wget -O "path/to/keycloak/modules/keycloak-chameleon-$version.jar" "$github_package"
```

## Acknowledgements

- [mschwartau/keycloak-custom-protocol-mapper-example](https://github.com/mschwartau/keycloak-custom-protocol-mapper-example) provided much of the reference implementation.
