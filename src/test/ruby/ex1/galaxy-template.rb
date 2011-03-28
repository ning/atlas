
# an external template defining galaxy! (see system-template.rb)

server "geponsole", :base => "ubuntu-small",
                    :install => ["chef:gepo-2.7", "chef:gonsole-2.7"],
                    :order => 10 # not yet supported
