#!/bin/bash
docker compose run -T --rm service python /work/printmeta.py $@
