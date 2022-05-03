#!/bin/bash

./build.sh

rm total.zip

zip -r total.zip buildzip.sh build.sh makegroups.sh splitfasta.py test.fasta release.jar README.md
zip -r total.zip .git .gitignore
zip -r total.zip src/

cd ../data/

zip -r ../code/total.zip samples/0*.zip samples/faulty.zip
