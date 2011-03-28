require 'java'
require 'open-uri'

module Atlas

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