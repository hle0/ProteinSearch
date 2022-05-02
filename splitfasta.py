#!/usr/bin/env python3
# FASTA data from uniprot comes in one giant file (don't ask me why)
# This splits it into some more manageable pieces
# You don't need to grade this (see README.md)

import os

def filename(line):
    d = 'samples/originals'
    if not os.path.isdir(d):
        os.mkdir(d)
    return d + '/' + line.split('|')[1] + '.fasta'

def endless():
    i = 0
    while True:
        yield i
        i = i + 1

line = input()
for i in endless():
    with open(filename(line), 'w+') as f:
        tmp = True
        while tmp or not line.startswith('>'):
            tmp = False
            f.write(line + '\n')
            line = input()
