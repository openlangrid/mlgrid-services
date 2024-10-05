#!/bin/bash
model=${1:-flux-dev}
shift
docker compose run -T --rm service python test_${model}.py $@
