#!/usr/bin/env bash
version=$(grep version pom.xml | grep -v '<?xml' | grep '<version>'|head -n 1|awk '{print $1}'| cut -d'>' -f 2 | cut -d'<' -f 1)
echo "##vso[task.setvariable variable=version]$version"