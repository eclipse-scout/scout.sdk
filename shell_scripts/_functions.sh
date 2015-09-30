#!/bin/bash

function processError {
  SED_OUT=$?
  if [ $SED_OUT -ne 0 ]; then
    echo "EXIT: "$SED_OUT
    exit $SED_OUT
  fi
}