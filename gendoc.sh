#!/bin/sh
mkdir -p target/site
pandoc --toc --html5 -f markdown -t html -c pandoc.css -o target/site/index.html \
       --template src/site/pandoc/html.FORMAT \
       src/site/pandoc/index.md \
       src/site/pandoc/building.md \
       src/site/pandoc/configuring.md \
       src/site/pandoc/running.md
cp src/site/pandoc/pandoc.css target/site

