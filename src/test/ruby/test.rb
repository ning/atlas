

require '../../main/ruby/atlas/template'

a = Atlas::Template::SystemTemplateParser.new 'ex1/system-template.rb'
model = a.parse

puts model