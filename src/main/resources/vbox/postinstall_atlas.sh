#!/bin/sh

# WARNING: This script is for Ubuntu

# Modified from Vagrant's postinstall.sh
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

ATLASUSER="atlasuser"

# Apt-install various things necessary for Ruby, guest additions,
# etc., and remove optional things to trim down the machine.
apt-get -y update
apt-get -y remove apparmor
apt-get -y install linux-headers-$(uname -r) build-essential
apt-get -y install zlib1g zlib1g-dev libxml2 libxml2-dev libxslt-dev libssl-dev openssl libreadline5-dev
apt-get clean

apt-get -y install vim emacs-nox git
apt-get -y install openssh-server openssh-client

# install ruby and gems
apt-get -y install ruby ruby-dev libopenssl-ruby rdoc ri irb wget ssl-cert rubygems

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


# Install Chef & Puppet
gem install chef --no-ri --no-rdoc
gem install puppet --no-ri --no-rdoc

# Setup .ssh folder
cd
mkdir ~/.ssh
chmod 700 ~/.ssh
cd ~/.ssh
touch authorized_keys
cd
chown -R ${ATLASUSER} ~/.ssh


# Install VirtualBox guest additions
# VBOX_VERSION=$(cat /home/${ATLASUSER}/.vbox_version)
VBOX_VERSION="4.1.0"
cd /tmp
wget http://download.virtualbox.org/virtualbox/$VBOX_VERSION/VBoxGuestAdditions_$VBOX_VERSION.iso
mount -o loop VBoxGuestAdditions_$VBOX_VERSION.iso /mnt
sh /mnt/VBoxLinuxAdditions.run
umount /mnt
rm VBoxGuestAdditions_$VBOX_VERSION.iso

# Remove items used for building, since they aren't needed anymore
apt-get -y remove linux-headers-$(uname -r) build-essential
apt-get -y autoremove

# Zero free space to aid VM compression
dd if=/dev/zero of=/EMPTY bs=1M
rm -f /EMPTY

# Removing leftover leases and persistent rules
echo "cleaning up dhcp leases"
rm /var/lib/dhcp3/*

# Make sure Udev doesn't block our network
# http://6.ptmc.org/?p=164
echo "cleaning up udev rules"
rm /etc/udev/rules.d/70-persistent-net.rules
mkdir /etc/udev/rules.d/70-persistent-net.rules
rm -rf /dev/.udev/
rm /lib/udev/rules.d/75-persistent-net-generator.rules

# more networking stuff

echo "Modifying /etc/network/interfaces"

echo "
# The lookback network interface
auto lo
iface lo inet loopback

# The primary network interfaces

# Host-only adapter
auto eth0
iface eth0 inet dhcp
pre-up sleep 2

# Bridged adapter
auto eth1
iface eth1 inet dhcp
pre-up sleep 2

# NAT
auto eth2
iface eth2 inet dhcp
pre-up sleep 2
" > /etc/network/interfaces

sleep 1

dhclient -r
sleep 1
dhclient
sleep 1

ifconfig

exit
