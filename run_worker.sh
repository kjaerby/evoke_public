#!/bin/bash
export MEGA_CMD_TTL=3600

mega-ftp /

java -cp "target/evoke-1.0-SNAPSHOT-jar-with-dependencies.jar" moe.evoke.application.backend.mirror.DistributionWorker

