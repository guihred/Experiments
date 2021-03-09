import pefile
import sys
pe = pefile.PE(sys.argv[1])
for section in pe.sections:
    print("%s" % (section.Name))
