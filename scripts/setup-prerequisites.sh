#!/usr/bin/env bash

set -e

echo "Checking development prerequisites..."

check_command () {
  if ! command -v $1 &> /dev/null
  then
    echo "$1 is not installed."
    echo "Please install $1 before continuing."
    exit 1
  else
    echo "$1 is installed."
  fi
}

check_command docker
check_command docker-compose
check_command make

echo ""
echo "All required tools are installed."
echo "You can now run the project with:"
echo ""
echo "make build"
echo "make run"
