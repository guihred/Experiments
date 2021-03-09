import pefile
import time
import sys

pe = pefile.PE(sys.argv[1])
timestamp = pe.FILE_HEADER.TimeDateStamp
print(time.strftime("%Y-%m-%d %H:%M:%S",time.localtime(timestamp)))
