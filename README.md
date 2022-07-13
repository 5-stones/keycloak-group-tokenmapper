# Keycloak OIDC protocol token Group mapper

This token mapper allows for the mapping of user group data into the token. It
includes granular control of what properties from the group object you want to
map.

![Screen Shot](./config.png)

## Installation

1. Build the jar:
	```
	mvn clean install
	```
2. Copy the jar produced in `target/` to your `providers` directory (for Quarkus)
	or `standalone/deployments` directory (for legacy) and rebuild/restart keycloak.
