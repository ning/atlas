require 'java'
require 'open-uri'

module Atlas
  include_package "com.ning.atlas.template"

  def self.parse path, name="__ROOT__"
    RootParser.new(name, path).__parse
  end

  class RootParser

    def initialize name, path
      @name, @path = name, path
      @children, @environments = [], []
      @template = open(path).read
    end

    def __parse
      eval @template, binding, @path, 1
      root = Atlas::SystemTemplate.new @name
      @children.each {|t, cnt| root.addChild(t, cnt)}
      Atlas::Root.new root, @environments
    end

    def server name, args={}, &block
      @children << [ServerParser.new(name, args, block).__parse, args[:count] || 1]
    end

    def system name, args={}, &block
      st = if args[:external] then
             Atlas.parse(args[:external], name).deployment_root
           else
             SystemParser.new(name, args, block).__parse
           end
      @children << [st, args[:count] || 1]
    end

    def environment name, args={}, &block
      @environments << EnvironmentParser.new(name, args, block).__parse
    end
  end

  class EnvironmentParser

    def initialize name, args, block
      @name, @args, @block = name, args, block
    end

    def __parse
      Atlas::Environment.new @name
    end
  end

  class SystemParser
    def initialize name, args, block
      @name, @args, @block = name, args, block
      @children = []
    end

    def __parse
      instance_eval &@block
      s = Atlas::SystemTemplate.new @name
      @args.each do |k,v|
        s.send(k, v) if s.respond_to? "#{k}=".to_sym
      end
      @children.each {|child, cnt| s.addChild(child, cnt) }
      s
    end

    def system name, args={}, &block
      if args[:external]
      else
        @children << [SystemParser.new(name, args, block).__parse, (args[:count] || 1)]
      end
    end

    def server name, args={}, &block
      @children << [ServerParser.new(name, args, block).__parse, (args[:count] || 1)]
    end
  end

  class ServerParser
    def initialize name, args, block
      @name, @args, @block = name, args, block
    end

    def __parse
      s = Atlas::ServerTemplate.new @name
      @args.each do |k,v|
        setter = "#{k}=".to_sym
        s.send(setter, v) if s.respond_to? setter
      end
      s
    end
  end
end
