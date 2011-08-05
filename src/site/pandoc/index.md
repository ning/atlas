# Introduction

Atlas is a tool to automatically spin up complete environments consisting of multiple machines
and software installed on them. These environments are described via configuration files which
are fed to Atlas, and Atlas then automatically procures the necessary resources and installs
the base machines and the software running on them.

Atlas is currently able to spin up environments on EC2 and VirtualBox.

Atlas is being built by various folks, mostly from [Ning](http://www.ning.com), and is made available under the [Apache License, 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

# Where to get it

Atlas is on github: [https://github.com/ning/atlas](https://github.com/ning/atlas).

# 5 minute quick start

Build it:

    rake package

Create the [environment specification](#environment-specification) and
[system specification](#system-specification).

Provision stuff:

    ./target/atlas -e <environment specification file> -s <system specification file> provision

Initialize stuff:

    ./target/atlas -e <environment specification file> -s <system specification file> initialize

Start stuff:

    ./target/atlas -e <environment specification file> -s <system specification file> start

