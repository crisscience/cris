import sys
import re
import json
import os

# this code is intended to be called with data passed via stdin and returned via stdout

# the next line tells the script to read from stdin
#input_json = sys.stdin.read()

#try:
#  data=json.loads(input_json)

#except ValueError:
#  sys.exit("ERROR: sys.argv[1] was not in JSON format")

#
# output "Hello World"
#

data = {}

data["Message"]="Hello World"
data["isValid"] = "True"

outputString=json.dumps(data)
print outputString