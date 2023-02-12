#!/bin/bash

TEXT="${1:-私はヴォルフガングでベルリンに住んでいます}"
shift

docker-compose run --rm service python run.py "$TEXT" --textLang=ja --targetLang=en "$@"
