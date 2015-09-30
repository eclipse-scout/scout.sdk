#!/bin/bash
# Check correct copyright headers

mvn license:check -Dlicense_check $*
mvn license:check -Dp2 -Dlicense_check $*