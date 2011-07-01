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
      @root = com.ning.atlas.Environment.new @name
      eval @template, binding, @path, 1
      @children.each {|t| @root.addChild(t)}
      @root
    end

    def environment name, &block
      @children << EnvironmentParser.new(name, @root, block).__parse
    end
  end

  class EnvironmentParser

    def initialize name, parent, block
      @name, @parent, @block = name, parent, block
    end

    def __parse
      @env = com.ning.atlas.Environment.new @name, @parent.provisioner, @parent.initializers
      instance_eval &@block
      @env
    end

    def environment name, &block
      @env.addChild(EnvironmentParser.new(name, @env, block).__parse)
    end

    def base name, args={}
      attr = args.inject(Hash.new) {| a, (k, v)| a[k.to_s] = v.to_s; a}
      base = com.ning.atlas.Base.new(name, @env, attr)

      if args[:init]
        args[:init].each {|v| base.addInit(v)}
      end

      @env.addBase(base)
    end

    def provisioner clazz, args={}
      attr = args.inject(Hash.new) {| a, (k, v)| a[k.to_s] = v.to_s; a}
      p =  com.ning.atlas.JrubyHelper.create(clazz, attr)
      args.each do |k, v|
        sym = "#{k}=".to_sym
        p.send(sym, v) if p.respond_to? sym
      end
      @env.provisioner = p
    end

    def initializer name, clazz, args={}
      attr = args.inject(Hash.new) {| a, (k, v)| a[k.to_s] = v.to_s; a}
      init =  com.ning.atlas.JrubyHelper.create(clazz, attr)

      args.each do |k, v|
        sym = "#{k}=".to_sym
        init.send(sym, v) if init.respond_to? sym
      end

      @env.addInitializer(name, init)
    end

    def installer name, clazz, args={}
      attr = args.inject(Hash.new) {| a, (k, v)| a[k.to_s] = v.to_s; a}
      installer =  com.ning.atlas.JrubyHelper.create(clazz, attr)

      args.each do |k, v|
        sym = "#{k}=".to_sym
        installer.send(sym, v) if installer.respond_to? sym
      end

      @env.addInstaller(name, installer)
    end


    def system *args
      #no-op
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
      s.my = @args.inject(Hash.new) {| a, (k, v)| a[k.to_s] = v; a}
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
      s.my = @args.inject(Hash.new) {| a, (k, v)| a[k.to_s] = v; a}
      s
    end
  end
end
