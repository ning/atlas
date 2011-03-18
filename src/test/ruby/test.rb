

require '../../main/ruby/atlas/system_template_parser'

a = Atlas::SystemTemplateParser.new 'ex1/system-template.rb'
model = a.parse

puts model