#!/bin/bash
T=${1:-tf}
shift
docker compose run -T --rm service python test_${T}.py $@
