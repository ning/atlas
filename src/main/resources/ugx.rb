#!/usr/bin/env ruby
require 'optparse'
require 'open-uri'
require 'fileutils'
require 'tempfile'

def clean
  FileUtils.rm_rf Flags.root
end

def deploy
  FileUtils.mkdir_p Flags.root
  file = Tempfile.new "ugx"
  open Flags.binary_url do |io|
    file.open do |fio|
      fio.write io.read
    end
  end
  `#{Flags.tar} -C #{Flags.root} -zxf #{file.path}`
  xndeploy = "#{Flags.root}/bin/xndeploy"
  unless FileTest.executable? xndeploy
    xndeploy = "/bin/sh #{xndeploy}"
  end
  command = "#{xndeploy} --base #{Flags.root} --binaries #{Flags.binary_repo_base} --config-path #{Flags.config_path} --repository #{Flags.repository}"
  system(command)
end

def start
  system("#{Flags.root}/bin/launcher start")
end

def stop
  system("#{Flags.root}/bin/launcher stop")
end

def status
  system("#{Flags.root}/bin/launcher status")
end

class Whee
  attr_accessor :tar, :host, :root, :binary_repo_base
  attr_accessor :repository, :binary_url, :config_path
end

def load_defaults
  flags = Whee.new
  open(".ugxrc") do |rc|
    while line = rc.gets
      key, value = line.split("=")
      unless line =~ /#.+/ or line =~ /^\s*$/
        flags.send("#{key.strip}=".to_sym, value.strip)
      end
    end
  end
  flags
end

Flags = load_defaults

opts = OptionParser.new do |opts|
  opts.on("-r", "--root=ROOT", "Deployment Root") do |r|
    Flags.root = r
  end

  opts.on("-b", "--binary-url=URL", "URL to galaxy binary") do |url|
    Flags.binary_url = url
  end

  opts.on("-H", "--host=HOST", "Host or IP to bind to") do |ip|
    Flags.host = ip
  end

  opts.on("-c", "--config-path=PATH", "Config Path") do |c|
    Flags.config_path = c
  end

  opts.on("-B", "--binary-repo=URL", "Binary Repo Base URL") do |b|
    Flags.binary_repo_base = b
  end

  opts.on("-R", "--repository=URL", "Config Repo") do |r|
    Flags.repository = r
  end

end

command = opts.parse(ARGV)[0] || "help"

if command == "help"
  puts opts
  exit
end

send command.to_sym
