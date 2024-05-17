#!/bin/bash
docker-compose -f /home/springboot/docker-compose.yml pull $@
#docker-compose -f /home/springboot/docker-compose.yml  down $@
#docker-compose -f /home/springboot/docker-compose.yml  up $@  -d
docker-compose -f /home/springboot/docker-compose.yml up -d --build $@
