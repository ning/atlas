#!/bin/sh

if [ -z $GENDOC_TMP ]; then
    GENDOC_TMP="target/site"
    mkdir -p $GENDOC_TMP
fi

pandoc --toc --html5 -f markdown -t html -c pandoc.css --template src/site/pandoc/template.html \
       -o $GENDOC_TMP/index.html \
       src/site/pandoc/index.md \
       src/site/pandoc/building.md \
       src/site/pandoc/configuring.md \
       src/site/pandoc/running.md \
       src/site/pandoc/resources.md
cp src/site/pandoc/pandoc.css $GENDOC_TMP

