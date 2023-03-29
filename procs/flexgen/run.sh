#!/bin/bash

TEXT="${1:-My name is Wolfgang and I live in Berlin}"
shift

echo "$TEXT" | docker-compose run --rm service python /workspace/FlexGen/apps/chatbot.py --model facebook/opt-6.7b 
