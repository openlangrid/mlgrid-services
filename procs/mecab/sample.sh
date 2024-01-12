#!/bin/bash
docker compose run -T --rm service python3 sample.py $@
#echo "Amazon Web Services（アマゾン ウェブ サービス、略称：AWS）とは、Amazon.comにより提供されているクラウドコンピューティングサービスである。" | docker compose run -T --rm service mecab
#docker compose run --rm service bash
