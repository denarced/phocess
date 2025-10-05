#!/usr/bin/env bash

set -eu

print_help() {
    echo "Usage: ./format-on-save.sh [option]"
    echo "Format code on changes and build the project."
    echo ""
    echo "Options:"
    echo "  -p, --package-only  Format and build, without running the built jar."
    echo "  -h, --help          Display this help message."
    echo ""
    echo "Default behavior (no options): format code, build, and run the built jar."
}

if [ "$#" -gt 0 ] && { [ "$1" == "--help" ] || [ "$1" == "-h" ]; }; then
    print_help
    exit 0
fi

check_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "$1: not found, quitting"
        exit 10
    fi
}

for cmd in entr find mvn; do
    check_command "$cmd"
done

COMMAND_TO_RUN="mvn prettier:write && mvn package && java -jar target/*.jar"

if [ "$#" -gt 0 ] && { [ "$1" == "--package-only" ] || [ "$1" == "-p" ]; }; then
    COMMAND_TO_RUN="mvn prettier:write && mvn package"
fi

find ./src -type f \( -name '*.java' -o -name '*.json' \) |
    entr -r sh -c "$COMMAND_TO_RUN"
