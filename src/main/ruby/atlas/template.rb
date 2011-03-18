require 'java'

module Atlas

  module Template
    include_package "com.ning.atlas.template"

    class SystemTemplateParser

      def initialize template_path
        @template = File.read template_path
        @template_path = template_path
        @last = false
      end

      def parse
        eval(@template, binding, @template_path, 1)
        @root
      end


      # this is the little language for creating the system templates

      def system name, args={}

        sys = Atlas::Template::SystemTemplate.create(name, args.inject({}) { |h, (k, v)| h[k.to_s] = v.to_s; h })

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
        serv = Atlas::Template::ServerTemplate.create(name, args.inject({}) { |h, (k, v)| h[k.to_s] = v.to_s; h })
        cnt = args[:count] || 1
        @last.addChild(serv, cnt);
      end

      def aka from, to

      end

      def override name, value

      end

    end
  end
end