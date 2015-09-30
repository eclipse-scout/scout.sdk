#!/bin/bash
# Set correct copyright headers

mvn license:format $*
mvn license:format -Dp2 $*