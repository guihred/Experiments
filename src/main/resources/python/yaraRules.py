import yara
import sys
import os
import pathlib
from pathlib import Path

filename = Path('../rules').resolve()

fil=sys.argv[1]

rules = yara.load_rules(rules_rootpath=filename)

matches = rules.match(fil)
for match in matches:
        print ("%s\t" % match)