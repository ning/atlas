# Running

Atlas' commandline 

Invoking atlas without any arguments will show the basic help

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ atlas <options> <command>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Available options are:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-e, --env, --environment   Environment specification file
-s, --sys, --system        System specification file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Atlas requires both these options on the command line: the path to the
[environment specification](#environment-specification) and the path to the
[system specification](#system-specification). Everything else is configured within those two files.

Commands are:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
provision    Provisions machines/instances.
initialize   Initializes the provisioned machines/instances.
install      Installs services onto the initialized machines/instances.
start        Alias for install.
help         Shows basic commandline help.
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

When running against EC2, you should also make sure that before invoking atlas, the ssh key is
registered, e.g.:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ ssh-add my-ec2-keypair.pem
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Output

TBD
