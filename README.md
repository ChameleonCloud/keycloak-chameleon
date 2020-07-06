# Chameleon Keycloak extension

* [protocol-mapper](protocol-mapper): a custom OpenID protocol mapper, which
  returns a list of the user's linked federated identity provider (aliases) on
  a new user-defined claim name. In order for this to be useful with multiple
  providers, you must configure it as a multi-value claim.

## Build

```shell
make build
```

### Checking the access token

    curl -d 'client_id=example-realm-client' -d 'username=jdoe' -d 'password=password' -d 'grant_type=password' 'http://localhost:11080/auth/realms/example-realm/protocol/openid-connect/token'

Response should be like:

    {
      "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvVXp0bXNyWmF4YUpHY0xKY2l3cV9uM3c1Rm12QVpYV2xMSDFtWGJEeGpNIn0.eyJqdGkiOiJhNTYzOGNhZC04MDQwLTRjMTItYjk1Ny0xZTM3ZTk2MGU1ZTMiLCJleHAiOjE1NDIzMTM1NjcsIm5iZiI6MCwiaWF0IjoxNTQyMzEzMjY3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjExMDgwL2F1dGgvcmVhbG1zL2V4YW1wbGUtcmVhbG0iLCJhdWQiOiJleGFtcGxlLXJlYWxtLWNsaWVudCIsInN1YiI6ImZjNzI0NGVkLTg4Y2EtNGYwOC05MGI1LWUxODk5NzhhZTQxYyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImV4YW1wbGUtcmVhbG0tY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiMWQzNjlhNjQtZTM5My00NjEzLWI4N2QtOTgwZDA2Y2U0Y2M1IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6W10sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiSm9obiBEb2UiLCJncm91cHMiOltdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJqZG9lIiwiZ2l2ZW5fbmFtZSI6IkpvaG4iLCJmYW1pbHlfbmFtZSI6IkRvZSIsImV4YW1wbGUiOnsibWVzc2FnZSI6ImhlbGxvIHdvcmxkIn19.b270PBBV498Tb3pL1MStP8QIHGXNCzNupVKDAoyStykf4PHewUPMNi_UvmRFP8QUIAIdXfdt3XQ5S4X9ALImmc4Ik92SUT3scsLrZVEtt21Spv6C73HUjJ-vYNaQ6-Rsb0lUpMhrEObYEiDHXCAobwlLcxwTbZbXOJrxBKwflibSfVxkYUD_DDsT2EW4vY1QVfWEa3IcuLNb--fmrbKoEE_Z20_X808jIsNruIijSfADHxDolg0-QPw95_SjUqlQThvWlVVbT12Xe5YsTKbayKDCP__UqQ0DCetOmnEFHkkG6PxPMLOclDwCg68blry4QrYitmmH5IHsKkvs-DJQeA",
      "expires_in": 300,
      "refresh_expires_in": 1800,
      "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJlNzA1ZGUwNC04NGFiLTRhOGMtYTRhMi03NGEwYzAxZGJhNGMifQ.eyJqdGkiOiJhOTBmMzJhYi0yOWM4LTRiNzctYTBiYS1mMDc1ZjE5NmU0ODEiLCJleHAiOjE1NDIzMTUwNjcsIm5iZiI6MCwiaWF0IjoxNTQyMzEzMjY3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjExMDgwL2F1dGgvcmVhbG1zL2V4YW1wbGUtcmVhbG0iLCJhdWQiOiJleGFtcGxlLXJlYWxtLWNsaWVudCIsInN1YiI6ImZjNzI0NGVkLTg4Y2EtNGYwOC05MGI1LWUxODk5NzhhZTQxYyIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJleGFtcGxlLXJlYWxtLWNsaWVudCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjFkMzY5YTY0LWUzOTMtNDYxMy1iODdkLTk4MGQwNmNlNGNjNSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIn0.BhZOitnAbxSRHvzXR4KS1eyZRTgnMhcYSCikKbLXw2I",
      "token_type": "bearer",
      "not-before-policy": 0,
      "session_state": "1d369a64-e393-4613-b87d-980d06ce4cc5",
      "scope": "profile email"
    }

Then copy the `access_token` value and decode it, e.g. by using [jwt.io](https://jwt.io/). You'll
get something like the following:

    {
      "jti": "a5638cad-8040-4c12-b957-1e37e960e5e3",
      "exp": 1542313567,
      "nbf": 0,
      "iat": 1542313267,
      "iss": "http://localhost:11080/auth/realms/example-realm",
      "aud": "example-realm-client",
      "sub": "fc7244ed-88ca-4f08-90b5-e189978ae41c",
      "typ": "Bearer",
      "azp": "example-realm-client",
      "auth_time": 0,
      "session_state": "1d369a64-e393-4613-b87d-980d06ce4cc5",
      "acr": "1",
      "allowed-origins": [],
      "realm_access": {
        "roles": [
          "offline_access",
          "uma_authorization"
        ]
      },
      "resource_access": {
        "account": {
          "roles": [
            "manage-account",
            "manage-account-links",
            "view-profile"
          ]
        }
      },
      "scope": "profile email",
      "email_verified": false,
      "name": "John Doe",
      "groups": [],
      "preferred_username": "jdoe",
      "given_name": "John",
      "family_name": "Doe",
      "example": {
        "message": "hello world"
      }
    }

## Acknowledgements

- [mschwartau/keycloak-custom-protocol-mapper-example](https://github.com/mschwartau/keycloak-custom-protocol-mapper-example) provided much of the reference implementation.
