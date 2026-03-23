#!/usr/bin/env python3
"""Simple file server for document downloads"""
import http.server
import os
import sys

os.chdir("/home/user/webapp/docs")

class DocHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Content-Disposition', 'attachment')
        super().end_headers()
    
    def log_message(self, format, *args):
        sys.stdout.write(f"{self.log_date_time_string()} - {format % args}\n")
        sys.stdout.flush()

port = 8899
print(f"Serving docs on port {port}")
sys.stdout.flush()
with http.server.HTTPServer(("0.0.0.0", port), DocHandler) as httpd:
    httpd.serve_forever()
