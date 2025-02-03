#!/bin/bash
model=${1:-videojp}
shift
docker compose run -T --rm service python test_${model}.py $@
