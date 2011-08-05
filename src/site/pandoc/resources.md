# Resources

## VirtualBox

To use the VirtualBox provisioner, a VirtualBox image is required.
Typically, such an image can be created using VirtualBox or VMWare,
and the appliance is then exported to an
[Open Virtualization Format](http://en.wikipedia.org/wiki/Open_Virtualization_Format) (OVF) file with the hard disk separate,
or an Open Virtualization Archive (OVA) with is a zipped copy of the hard disk and the OVF file.

Because creating a VirtualBox image can be a hassel, we have written a simple script
[createvm.sh](https://github.com/ning/atlas/blob/master/src/main/resources/vbox/createvm.sh)
that automates the creation of the bare virtual hardware.
Note that because the Linux kernel in Ubuntu Server is compiled with PAE, you need enable PAE support on the VM
if you are running Ubuntu Server.

We have also provided a [postinstall_atlas.sh](https://github.com/ning/atlas/blob/master/src/main/resources/vbox/postinstall_atlas.sh)
script that can be run after installing the guest OS on the virtual machine.
The script also installs the 

## Getting Help

We have a [development mailing list](http://groups.google.com/group/atlas-dev).

