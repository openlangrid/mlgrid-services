#!/bin/bash
docker compose run --rm service python3 -m magicanimate.pipelines.animation --config /work/animation.yml
