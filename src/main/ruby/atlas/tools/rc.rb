require 'yaml'

module Atlas

  module Tools
    module RC

      def self.read_rc
        if File.exist? ".atlasrc"
          YAML.load_file(".atlasrc")
        elsif File.exists? "#{ENV['HOME']}/.atlasrc"
          YAML.load_file("#{ENV['HOME']}/.atlasrc")
        end
      end

    end
  end
end