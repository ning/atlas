# Running

Invoking atlas without any arguments will show the basic help

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ atlas

Missing one or both of environment or system specification paths
Option                                  Description
------                                  -----------
-e, --env, --environment                Environment specification file
-s, --sys, --system                     System specification file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Atlas only needs two arguments on the command line: the path to the
[environment specification](#environment-specification) and the path to the
[system specification](#system-specification). Everything else is configured within those two files.

When running against EC2, you should also make sure that before invoking atlas, the ssh key is
registered, e.g.:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ ssh-add my-ec2-keypair.pem
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Output

TBD
