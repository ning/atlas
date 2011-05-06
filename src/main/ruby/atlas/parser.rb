require 'java'
require 'open-uri'

module Atlas

  include_package "com.ning.atlas"

  def self.parse_system path, name="__ROOT__"
    RootSystemParser.new(name, path).__parse
  end

  def self.parse_env path, name="__ROOT__"
    RootEnvParser.new(name, path).__parse
  end


  #
  # Environment Parsing Stuff
  #

  class RootEnvParser
    def initialize name, path
      @name, @path = name, path
      @children = []
      @template = open(path).read
    end

    def __parse
      eval @template, binding, @path, 1
      root = Environment.new @name
      @children.each {|t| root.addChild(t)}
      root
    end
  end

  class EnvironmentParser

    def initialize name, args, block
      @name, @args, @block = name, args, block
    end

    def __parse
      com.ning.atlas.Environment.new @name
    end


    def system *args

    end
  end




  #
  # System Parsing Magic
  #

  class RootSystemParser

    def initialize name, path
      @name, @path = name, path
      @children = []
      @template = open(path).read
    end

    def __parse
      eval @template, binding, @path, 1
      root = com.ning.atlas.SystemTemplate.new @name
      @children.each { |t| root.addChild(t) }
      root
    end

    def server name, args={}, &block
      @children << ServerParser.new(name, args, block).__parse
    end

    def system name, args={}, &block
      st = if args[:external] then
             Atlas.parse_system(args[:external], name)
           else
             SystemParser.new(name, args, block).__parse
           end
      @children << st
    end


    def environment *args
      # system templates will ignore environment definitions so that you can put them
      # in the same file if you so choose
    end
  end

  class SystemParser
    def initialize name, args, block
      @name, @args, @block = name, args, block
      @children = []
    end

    def __parse
      instance_eval &@block
      s = com.ning.atlas.SystemTemplate.new @name
      @args.each do |k, v|
        sym = "#{k}=".to_sym
        s.send(sym, v) if s.respond_to? sym
      end
      @children.each { | child | s.addChild(child) }
      s
    end

    def system name, args={}, &block
      if args[:external]
      else
        @children << SystemParser.new(name, args, block).__parse
      end
    end

    def server name, args={}, &block
      @children << ServerParser.new(name, args, block).__parse
    end
  end

  class ServerParser
    def initialize name, args, block
      @name, @args, @block = name, args, block
    end

    def __parse
      s = com.ning.atlas.ServerTemplate.new @name
      @args.each do |k, v|
        setter = "#{k}=".to_sym
        s.send(setter, v) if s.respond_to? setter
      end
      s
    end
  end
end
