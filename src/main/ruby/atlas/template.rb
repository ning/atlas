require 'java'

module Atlas

  module Template
    include_package "com.ning.atlas.template"

    class SystemTemplateParser

      def initialize template_path
        @template = File.read template_path
        @template_path = template_path
        @last = false
        @aliases = {}
      end

      def parse
        eval @template, binding, @template_path, 1
        @root
      end


      # this is the little language for creating the system templates

      def aka args = {}
        @aliases = @aliases.merge args
      end

      def system name, args={}
        sys = Atlas::Template::SystemTemplate.new name

        if @last then
          cnt = args[:count] || 1
          @last.addChild(sys, cnt)
        else
          @root = sys
        end
        @last = sys

        yield if block_given?
      end

      def server name, args={}
        serv = Atlas::Template::ServerTemplate.new name
        serv.image = @aliases.fetch args[:image], args[:image]
        serv.bootstrap = @aliases.fetch args[:bootstrap], args[:bootstrap]

        cnt = args[:count] || 1
        @last.addChild(serv, cnt);
      end

      def override name, value

      end

    end
  end
end