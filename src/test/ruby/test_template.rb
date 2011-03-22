

require "../../main/ruby/atlas/template.rb"

p = Atlas::Template::SystemTemplateParser.new "ex1/system-template.rb"
t = p.parse