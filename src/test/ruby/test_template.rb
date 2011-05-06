require "/Users/brianm/src/atlas/src/main/ruby/atlas/parser.rb"
require "/Users/brianm/src/atlas/target/atlas-0.0.1-SNAPSHOT.jar"
require "/Users/brianm/.m2/repository/com/google/guava/guava/r09/guava-r09.jar"

puts Atlas.parse_system("/Users/brianm/src/atlas/src/test/ruby/ex1/system-template.rb")

puts Atlas.parse_env("/Users/brianm/src/atlas/src/test/ruby/ex1/simple-environment.rb")

