import pefile
import sys
mal_file = sys.argv[1]
pe = pefile.PE(mal_file)

if hasattr(pe, 'DIRECTORY_ENTRY_EXPORT'):
    for exp in pe.DIRECTORY_ENTRY_EXPORT.symbols:
        print("%s" % exp.name)
