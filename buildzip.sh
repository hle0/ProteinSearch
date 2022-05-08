#!/bin/bash

./build.sh

rm total.zip samples.zip samples-small.zip

zip -r total.zip buildzip.sh build.sh makegroups.sh splitfasta.py test.fasta release.jar README.md
zip -r total.zip .git .gitignore
zip -r total.zip src/

cd ../data/

zip -r ../code/samples.zip samples/0*.zip samples/faulty.zip
zip -r ../code/samples.zip data/uniprot_sprot.multifasta

zip -r ../code/samples-small.zip samples/0{1,2,3}.zip samples/faulty.zip
