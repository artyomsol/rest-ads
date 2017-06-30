Build Status

Goal of example is to show how create reactive REST services on Typesafe stack with Akka and Elasticsearch

Example contains complete REST service for entity interaction.
Features:

    CRUD operations
    Entity partial updates
    CORS support
    Test coverage with ScalaTest
    Ready for Docker

Requirements

    JDK 8 (e.g. http://www.oracle.com/technetwork/java/javase/downloads/index.html);
    sbt (http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html);


Configuration

    Set database/http/CORS settings on application config and link it with JVM options i.e.:
    -Dconfig.file=/full/path/to/config/local.conf -Dlogback.configurationFile=\"file:///full/path/to/config/logback.xml\""


Run tests

    To run tests, call:

    sbt test


Run application

    To run application, call:

        sbt run


Run in Docker

    Get Elasicsearch image from repo

        docker pull docker.elastic.co/elasticsearch/elasticsearch:5.4.3

    Generating application docker image and publishing on localhost:

        sbt docker:publishLocal
        cp -r ./target/docker/stage ./docker/
        cd ./docker
        docker-compose build
        docker-compose up
