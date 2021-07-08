import imgkit
import sys
url=sys.argv[1]
fil=sys.argv[2]
options = {
    'format': 'png',
    'javascript-delay': 20000,
    'custom-header' : [
        ('Authorization', 'Basic Z3VpbGhlcm1lLmhtZWRlaXJvczozMS1TQU5qdUlDSEk=')
    ]
}

imgkit.from_url(url, fil, options=options)