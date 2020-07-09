# Chameleon Keycloak extension

This single artifact contains a few extensions to Keycloak:

* A custom OpenID protocol mapper, which
  returns a list of the user's linked federated identity provider (aliases) on
  a new user-defined claim name. In order for this to be useful with multiple
  providers, you must configure it as a multi-value claim.
* A custom Keycloak theme that adds Chameleon branding and hides
  the username/password login flow. The theme also contains the terms and
  conditions for use of the Chameleon platform.
* A custom required action that requires a user to update their profile
  and set required fields that are non-standard.
* A custom required action that displays an additional notification message to
  users in a certain state.

## Build

```shell
make build
```

### Publishing new versions

This package is currently published to GitHub Packages.

```shell
make publish
```

## Acknowledgements

- [mschwartau/keycloak-custom-protocol-mapper-example](https://github.com/mschwartau/keycloak-custom-protocol-mapper-example) provided much of the reference implementation.
