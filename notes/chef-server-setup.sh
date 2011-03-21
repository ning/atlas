#!/bin/sh
# set up a chef-server
# http://wiki.opscode.com/display/chef/Package+Installation+on+Debian+and+Ubuntu

echo "deb http://apt.opscode.com/ `lsb_release -cs` main" | sudo tee /etc/apt/sources.list.d/opscode.list

wget -qO - http://apt.opscode.com/packages@opscode.com.gpg.key | sudo apt-key add -

sudo apt-get update

sudo apt-get -y install libopenssl-ruby
sudo apt-get -y install ruby-dev
sudo apt-get -y install build-essential
sudo apt-get -y install libnet-ssh-multi-ruby
sudo apt-get -y install libhighline-ruby
sudo apt-get -y install libfog-ruby

# debconf preseed bits
sudo apt-get install chef chef-server

