#!/bin/bash
pkill -f "java -jar ./build/libs/mlgrid-services-"
git pull
./gradlew build -x test
./gradlew --stop
nohup java -jar ./build/libs/mlgrid-services-0.0.1-SNAPSHOT.jar &> mlgrid-services.log &

