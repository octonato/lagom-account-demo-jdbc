#!/bin/sh

# this script will create and configure the Cassandra docker container
set -e

docker-compose stop
docker-compose rm
docker-compose up -d

until cat cassandra-schema.cql | cqlsh; do
  echo "cqlsh: Cassandra is unavailable - retry later"
  sleep 2
done 