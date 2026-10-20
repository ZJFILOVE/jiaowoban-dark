#!/usr/bin/env python3
"""Read current UI or tap a named node on an explicitly selected ADB device."""
import argparse
import re
import subprocess
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--tap', help='Exact text, content description, or resource id')
args = parser.parse_args()

def adb(*parts):
    return subprocess.check_output(['adb', '-s', args.serial, *parts], text=True)

adb('shell', 'uiautomator', 'dump', '/data/local/tmp/jiaowoban-ui.xml')
root = ET.fromstring(adb('shell', 'cat', '/data/local/tmp/jiaowoban-ui.xml'))
nodes = list(root.iter('node'))
if args.tap:
    matches = [n for n in nodes if args.tap in
               (n.get('text'), n.get('content-desc'), n.get('resource-id'))]
    matches = [n for n in matches if n.get('enabled') == 'true']
    bounds = [list(map(int, re.findall(r'\d+', n.get('bounds', '')))) for n in matches]
    bounds = [b for b in bounds if len(b) == 4 and b[2] > b[0] and b[3] > b[1]]
    if len(bounds) != 1:
        raise SystemExit(f'Expected one visible match for {args.tap!r}, found {len(bounds)}')
    x1, y1, x2, y2 = bounds[0]
    print(adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2)))
else:
    for node in nodes:
        label = node.get('text') or node.get('content-desc')
        if label:
            print(label[:100], node.get('bounds'), node.get('resource-id'))
