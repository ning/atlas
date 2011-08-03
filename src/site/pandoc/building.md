# Building

The easiest way to build atlas, is to use [rake](http://rake.rubyforge.org/)
which usually comes with any decent ruby installation:

    rake package

This will invoke Maven and generate an executable ``atlas`` file.

You can also do this manually:

    mvn install
    cat target/atlas-*.jar >> target/atlas
    chmod +x target/atlas

