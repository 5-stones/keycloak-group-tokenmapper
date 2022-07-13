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

## Release

The standard release command for this project is:

```
yarn version
```

This command will:

1. Generate/update the Changelog
1. Bump the package version
1. Tag & pushing the commit

e.g.

```
yarn version --new-version 1.2.17
yarn version --patch // 1.2.17 -> 1.2.18
```

#### Why Yarn?

Why are we using yarn on a Java project? Because we have standard tooling around
Changelog generation and release based around `commitizen` and
`conventional-changelog`. And we do what we want.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.
