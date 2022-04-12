#!/bin/bash

ver=$(./gradlew :currentVersion | grep 'Project version:' | sed -r 's/Project version: (.*)-SNAPSHOT$/\1/')
echo "tabulate-$ver"