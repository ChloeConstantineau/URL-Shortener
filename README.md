# URL Shortener

This project is a small url shortener with a very simple slug generator.

## Requirements

- JDK 23
- sbt
- Docker

## Config

All of the default configurations can be found in the [application.conf](./src/main/resources/application.conf).
Most of the config are self explanatory except for the following:

| Name                          | Description                                                                                                                                                                             |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| slugGeneratorOptions.alphabet | [Optional] Provide your own alphabet which must have at least 3 characters, contain only single byte characters and unique characters. If left blank the default alphabet will be used. |
| counterKey                    | The name of the key in redis to be used for the slug generation.                                                                                                                        |

## Run

```shell
docker-compose up -d # Run necessary services
sbt run # Server with default port 8080
```

## Test

```shell
docker-compose up -d # Run necessary services
sbt test
```

## API Documentation

The app is set up with open api documentation with the path `/openapi` such as `http://localhost:8080/openapi`