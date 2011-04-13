#!/Users/brianm/.rvm/bin/jruby-1.6.0

require 'java'
require 'open-uri'
require 'prettyprint'

module Atlas
  include_package "com.ning.atlas.template"

  def self.parse path, name="__ROOT__"
    RootParser.new(name, path).__parse
  end

  class RootParser

    def initialize name, path
      @name = name
      @template = open(path).read
      @path = path
      @children = []
      @aliases = {}
      @spaces = []
    end

    def __parse
      eval @template, binding, @path, 1
      root = Atlas::SystemTemplate.new @name
      @children.each {|t, cnt| root.addChild(t, cnt)}
      Atlas::Root.new root, @spaces
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

    def aka args = {}
      @aliases = @aliases.merge args
    end

    def space name, args={}, &block
      @spaces << SpaceParser.new(name, args, block).__parse
    end
  end

  class SpaceParser

    def initialize name, args, block
      @name, @args, @block = name, args, block
    end

    def __parse
      Atlas::Space.new @name
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
__END__






  module Template
    include_package "com.ning.atlas.template"

    class SystemTemplateParser

      def initialize template_path
        @template      = File.read template_path
        @template_path = template_path

        # bits of state for the parser
        @last = [] # stack of nodes
        @roots   = [] # system roots
        @aliases = {} # value aliases to be substituted

        # done as lambda instead of method in order to keep from polluting DSL
        @add_node = lambda do |node, args, block|
          if @last.last then
            cnt = args[:count] || 1
            @last.last.addChild(node, cnt)
          else
            @roots << node
          end
          @last.push node
          block.call if block
          @last.pop
        end
      end

      def parse
        eval @template, binding, @template_path, 1
        @roots
      end

      # this is the little language for creating the system templates

      def space name, args={}
        # not sure how to use this yet
      end

      def aka args = {}
        @aliases = @aliases.merge args
      end


      def system name, args={}, &block
        if args[:external]
          raise "Not allowed to define contents of external system" if block

          block = lambda do
            ext_template = open(args[:external]).read
            eval ext_template, binding, args[:external], 1
          end

        end

        sys = Atlas::Template::SystemTemplate.new name
        @add_node.call sys, args, block
      end

      def server name, args={}
        server           = Atlas::Template::ServerTemplate.new name
        server.image     = @aliases.fetch args[:base], args[:base]
        server.bootstrap = @aliases.fetch args[:bootstrap], args[:bootstrap]

        @add_node.call server, args, lambda {}
      end

      #def override name, value
      #
      #end

    end
  end
end
