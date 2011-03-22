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
        eval(@template, binding, @template_path, 1)
        @root
      end


      # this is the little language for creating the system templates

      def aka args = {}
        @aliases = @aliases.merge args
      end

      def system name, args={}
        sys = Atlas::Template::SystemTemplate.create(name, __mungify(args))

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
        serv = Atlas::Template::ServerTemplate.create(name, __mungify(args))
        cnt = args[:count] || 1
        @last.addChild(serv, cnt);
      end

      def override name, value

      end

      private
      # ensure that all keys and values are strings, and replace values with aliases
      def __mungify args
        args.inject({}) { |h, (k, v)| h[k.to_s] = @aliases.fetch(v, v).to_s; h }
      end

    end
  end
end