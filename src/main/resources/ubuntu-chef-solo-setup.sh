#!/bin/sh
# set up a chef-server
# http://wiki.opscode.com/display/chef/Package+Installation+on+Debian+and+Ubuntu

# configure the opscode apt repo
echo "deb http://apt.opscode.com/ `lsb_release -cs` main" | sudo tee /etc/apt/sources.list.d/opscode.list
wget -qO - http://apt.opscode.com/packages@opscode.com.gpg.key | sudo apt-key add -
sudo apt-get update

# set up answers to debconf questions
sudo apt-get -y install debconf-utils
echo "chef chef/chef_server_url string none" >> /tmp/debconf-answers.conf
sudo debconf-set-selections < /tmp/debconf-answers.conf

# things chef likes to have installed but doesn't explicitely depend on
sudo apt-get -y install build-essential
sudo apt-get -y install libopenssl-ruby
sudo apt-get -y install ruby-dev
sudo apt-get -y install libnet-ssh-multi-ruby
sudo apt-get -y install libhighline-ruby

#install chef
sudo apt-get -y install chef
sudo /etc/init.d/chef-client stop

# will hold all the roles, recipes, etc for chef-solo
sudo mkdir -p /var/chef-solo
