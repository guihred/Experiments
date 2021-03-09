import sys
import magic
fi=sys.argv[1]
ftype = magic.from_file(fi, mime=True)
print(ftype)

