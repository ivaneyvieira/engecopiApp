#!/usr/bin/env bash

git pull
./gradlew clean vaadinCompile vaadinUpdateWidgetset  vaadinThemeCompile  build -Pvaadin.productionMode

docker-compose down
docker-compose up 
#-d
