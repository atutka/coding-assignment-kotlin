# coding-assignment-kotlin

#### Used technologies ####

* kotlin
* spring boot 2.3.3
* spring data r2dbc
* spring webflux
* junit 5 and mockk for testing
* liquibase

#### How to run? ####

Start docker compose:
````
docker-compose up -d
````

Then run script build.sh
````
./build.sh
````
And then:
````
./run.sh
````

##### Postman #####

In directory `postman` is exported collection with example requests. 
You can import it to your Postman app.