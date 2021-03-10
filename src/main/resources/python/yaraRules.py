import yara
import sys
import os



def mycallback(data):
  print(data)
  yara.CALLBACK_CONTINUE

fil=sys.argv[1]
rules = yara.load_rules(rules_rootpath='..\rules')
matches = rules.match(fil, callback=mycallback)
