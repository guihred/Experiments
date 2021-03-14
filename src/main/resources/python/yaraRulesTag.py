import yara
import sys
import os
import pathlib
from pathlib import Path


def mycallback(data):
    if data['matches'] == True:
        if 'meta' in data.keys():
            print(data['rule'],"\t".join(data['meta'].values()))
        else:
            print(data['rule'],"\t".join(data.values()))

    yara.CALLBACK_CONTINUE


fil = sys.argv[1]
copy = sys.argv.copy()
copy.pop(0)
copy.pop(0)
modules = copy

for mod in modules:
    filename = Path('../rules/%s/' % mod).resolve()
    rules = yara.load_rules(rules_rootpath=filename)
    
    matches = rules.match(fil,callback=mycallback)

print('Done')
    
    
    
