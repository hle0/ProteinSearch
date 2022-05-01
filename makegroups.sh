#!/bin/bash
mkdir samples
cat data/uniprot_sprot.fasta | python3 splitfasta.py
cd samples/originals
ls | shuf > ../list.txt
cd ..
mkdir tmp
cd tmp
csplit -f '' -b '%02d' ../list.txt 10 50 150 550 1550 5000
for i in $(ls); do
    mkdir ../$i
    cat $i | xargs -I@ cp ../originals/@ ../$i/
done
cd ..
rm -rf tmp