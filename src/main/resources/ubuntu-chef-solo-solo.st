require "#{File.expand_path(File.dirname(__FILE__))}/s3_init.rb"

file_cache_path "/var/chef-solo"
cookbook_path "/var/chef-solo/cookbooks"
role_path "/var/chef-solo/roles"
recipe_url "$recipe_url$"
json_attribs "/etc/chef/node.json"
