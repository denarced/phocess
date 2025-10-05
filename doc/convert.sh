#!/bin/bash

D2_FILE="process.d2"

convert_d2() {
    d2 "$D2_FILE"
}

export -f convert_d2
export D2_FILE

echo "$D2_FILE" | entr -r bash -c 'convert_d2'
