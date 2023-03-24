#!/bin/bash

while true; do 
mvn compile exec:java -Dexec.mainClass="moe.evoke.application.backend.EvokeCLI" -Dexec.args="ipfsUpload './data'"
done

