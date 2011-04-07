aka "ubuntu-small" => "ami-a6f504cf"
chef_server_boot = <<-EOB
#!/bin/sh
# set up a chef-server
# http://wiki.opscode.com/display/chef/Package+Installation+on+Debian+and+Ubuntu

echo "deb http://apt.opscode.com/ `lsb_release -cs` main" | sudo tee /etc/apt/sources.list.d/opscode.list

wget -qO - http://apt.opscode.com/packages@opscode.com.gpg.key | sudo apt-key add -

sudo apt-get update

sudo apt-get -y install debconf-utils
echo "chef chef/chef_server_url string http://$(hostname -f):4000" >> /tmp/debconf-answers.conf
echo "chef-server-webui chef-server-webui/admin_password password chef" >> /tmp/debconf-answers.conf
echo "chef-solr chef-solr/amqp_password password chef" >> /tmp/debconf-answers.conf
sudo debconf-set-selections < /tmp/debconf-answers.conf

sudo apt-get -y install build-essential
sudo apt-get -y install libopenssl-ruby
sudo apt-get -y install ruby-dev
sudo apt-get -y install libnet-ssh-multi-ruby
sudo apt-get -y install libhighline-ruby


# debconf preseed bits
sudo apt-get -y install chef chef-server
EOB


system "chef" do
  server "chef-server", :base => "ubuntu-small",
                        :bootstrap => chef_server_boot
end