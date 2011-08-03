# Introduction

Atlas is a tool to automatically spin up complete environments consisting of multiple machines
and software installed on them. These environments are described via configuration files which
are fed to Atlas, and Atlas then automatically procures the necessary resources and installs
the base machines and the software running on them.
Atlas is current able to spin up environments on EC2 and VirtualBox.

# 5 minute quick start

Build it and make it executable:

    mvn install
    chmod +x target/atlas-<version>.jar

Create a [environment-specification] and [system-specification].
Run atlas:

    ./target/atlas-<version>.jar -e <environment specification file> -s <system specification file>

