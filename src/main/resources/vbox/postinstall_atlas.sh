#!/bin/sh

# Modified from Vagrant's postinstall.sh for Atlas

# http://vagrantup.com/license.html
# ---------------------------------
# The MIT License
#
# Copyright (c) 2010 Mitchell Hashimoto and John Bender
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
# documentation files (the “Software”), to deal in the Software without restriction, including without limitation 
# the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
# and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions
# of the Software.
#
# THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
# THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
# 
# ---------------------------------

# run as
# $ wget postinstall_atlas.sh
# $ chmod +x postinstall_atlas.sh
# $ sudo ./postinstall_atlas.sh

# ---------------------------------
# Variables

VBOX_VERSION="4.1.0"

# ---------------------------------

apt-get -y update
apt-get -y install openssh-server


# Remove this file to avoid dhclient issues with networking
rm -f /etc/udev/rules.d/70-persistent-net.rules

# Setup sudo to allow no-password sudo for "admin". Additionally,
# make "admin" an exempt group so that the PATH is inherited.
cp /etc/sudoers /etc/sudoers.orig
sed -i -e '/Defaults\s\+env_reset/a Defaults\texempt_group=admin' /etc/sudoers
sed -i -e 's/%admin ALL=(ALL) ALL/%admin ALL=NOPASSWD:ALL/g' /etc/sudoers

# Configure SSH specifically:
# This tells SSH not to look up the remote hostname for SSHing. This
# speeds up connection and helps when you're connecting with no outside
# internet connection.
echo 'UseDNS no' >> /etc/ssh/sshd_config


# Install VirtualBox guest additions
apt-get -y install linux-headers-$(uname -r) build-essential
cd /tmp
wget http://download.virtualbox.org/virtualbox/$VBOX_VERSION/VBoxGuestAdditions_$VBOX_VERSION.iso
mount -o loop VBoxGuestAdditions_$VBOX_VERSION.iso /mnt
sh /mnt/VBoxLinuxAdditions.run
umount /mnt
rm VBoxGuestAdditions_$VBOX_VERSION.iso


apt-get -y autoremove
apt-get clean


# Removing leftover leases and persistent rules
echo "cleaning up dhcp leases"
rm /var/lib/dhcp3/*

# Make sure Udev doesn't block our network
# http://6.ptmc.org/?p=164
echo "cleaning up udev rules"
mkdir /etc/udev/rules.d/70-persistent-net.rules
rm -rf /dev/.udev/
rm /lib/udev/rules.d/75-persistent-net-generator.rules

# More networking stuff

echo "Modifying /etc/network/interfaces"

echo "
# The lookback network interface
auto lo
iface lo inet loopback

# The primary network interfaces

auto eth0
iface eth0 inet dhcp
pre-up sleep 2

auto eth1
iface eth1 inet dhcp
pre-up sleep 2

" > /etc/network/interfaces

sleep 1

dhclient -r
dhclient


# Zero free space to aid VM compression
dd if=/dev/zero of=/EMPTY bs=1M
rm -f /EMPTY

sleep 2


exit