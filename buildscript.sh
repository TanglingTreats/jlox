#!/bin/bash

# Get dependencies
mvn dependency:resolve

# Compile package target
mvn clean package
