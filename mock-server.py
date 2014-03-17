#!/usr/bin/env python2

import urllib2 as url
import random
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler

class MockSphincter(BaseHTTPRequestHandler):
    
    def do_GET(self):
        self.send_response(200)
        self.end_headers()

        answers = ['SUCCESS', 'FAILED', 'NOT ALLOWED']

        self.wfile.write(random.choice(answers))

        return

if __name__ == '__main__':
    from BaseHTTPServer import HTTPServer
    server = HTTPServer(('localhost', 8000), MockSphincter)
    HTTPServer.serve_forever(server)
    














