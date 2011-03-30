aka "ubuntu-small" => "ami-a6f504cf"


system "chef" do
  server "server", :base => "ubuntu-small",
                   :bootstrap => <<-EOB.gsub(/^(\s*)(.*)/m, '\2').gsub(/^#{$1}/, '')
                                   #!/bin/sh
                                   # set up a chef-server
                                   # http://wiki.opscode.com/display/chef/Package+Installation+on+Debian+and+Ubuntu

                                   # configure the opscode apt repo
                                   echo "deb http://apt.opscode.com/ `lsb_release -cs` main" \
                                       | sudo tee /etc/apt/sources.list.d/opscode.list | logger
                                   wget -qO - http://apt.opscode.com/packages@opscode.com.gpg.key | sudo apt-key add - \
                                       | logger
                                   sudo apt-get update  | logger

                                   # set up answers to debconf questions
                                   sudo apt-get -y install debconf-utils  | logger
                                   echo "chef	chef/chef_server_url	string	http://$(hostname -f):4000" >> \
                                       /tmp/debconf-answers.conf  | logger
                                   echo "chef-server-webui	chef-server-webui/admin_password	password	chef" >> \
                                       /tmp/debconf-answers.conf  | logger
                                   echo "chef-solr	chef-solr/amqp_password	password	chef" >> \
                                       /tmp/debconf-answers.conf  | logger
                                   sudo debconf-set-selections < /tmp/debconf-answers.conf  | logger

                                   # things chef likes ot have installed but doesn't explicitely depend on
                                   sudo apt-get -y install libopenssl-ruby | logger
                                   sudo apt-get -y install ruby-dev | logger
                                   sudo apt-get -y install build-essential | logger
                                   sudo apt-get -y install libnet-ssh-multi-ruby | logger
                                   sudo apt-get -y install libhighline-ruby | logger
                                   sudo apt-get -y install libfog-ruby | logger

                                   #install chef server!
                                   sudo apt-get -y install chef chef-server | logger
                                 EOB
end