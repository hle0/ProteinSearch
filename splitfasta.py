#!/usr/bin/env python3
# FASTA data from uniprot comes in one giant file (don't ask me why)
# This splits it into some more manageable pieces
# Don't feel the need to grade this

import os

def filename(line):
    d = 'samples/originals'
    if not os.path.isdir(d):
        os.mkdir(d)
    return d + '/' + line.split('|')[1] + '.fasta'

line = input()
for i in range(10000):
    with open(filename(line), 'w+') as f:
        tmp = True
        while tmp or not line.startswith('>'):
            tmp = False
            f.write(line + '\n')
            line = input()
