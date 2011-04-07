#!/bin/sh
# set up a chef-server
# http://wiki.opscode.com/display/chef/Package+Installation+on+Debian+and+Ubuntu

# configure the opscode apt repo
echo "deb http://apt.opscode.com/ `lsb_release -cs` main" | sudo tee /etc/apt/sources.list.d/opscode.list
wget -qO - http://apt.opscode.com/packages@opscode.com.gpg.key | sudo apt-key add -
sudo apt-get update

# set up answers to debconf questions
sudo apt-get -y install debconf-utils
echo "chef chef/chef_server_url string ip-10-212-127-239.ec2.internal:4000" >> /tmp/debconf-answers.conf
sudo debconf-set-selections < /tmp/debconf-answers.conf

# things chef likes ot have installed but doesn't explicitely depend on
sudo apt-get -y install build-essential
sudo apt-get -y install libopenssl-ruby
sudo apt-get -y install ruby-dev
sudo apt-get -y install libnet-ssh-multi-ruby
sudo apt-get -y install libhighline-ruby

#install chef server!
sudo apt-get -y install chef 
