
task :default => :package

desc "clean and package up the executable"
task :package do
  sh "mvn clean package"
  File.open "target/atlas", "w"  do |f|
    f.write <<-EOS
#!/bin/sh

exec java -jar $0 "$@"


EOS
  end
  sh "cat target/atlas-*.jar >> target/atlas"
  sh "chmod +x target/atlas"
end

desc "run local tests"
task :test do
  sh "mvn clean test"
end

desc "run tests against ec2"
task "test-ec2" do
  sh "mvn -DRUN_EC2_TESTS=true clean test"

  # double check we didn't leak any ec2 instances
  sh "ec2din | grep INSTANCE | cut -f 2 | xargs ec2kill"
end

desc "kill all ec2 instances"
task "kill-ec2" do
   sh "ec2din | grep INSTANCE | cut -f 2 | xargs ec2kill"
end


desc "generate documenation and check it into gh-pages branch"
task "gen-docs" do
  require 'tmpdir'
  Dir.mktmpdir do |tmp|
    sh <<-EOS
      git branch --track gh-pages origin/gh-pages
      git clone -b gh-pages . #{tmp}
      pandoc -f markdown -t html -c pandoc.css -o #{tmp}/index.html \
             src/site/pandoc/index.md \
             src/site/pandoc/building.md \
             src/site/pandoc/configuring.md \
             src/site/pandoc/running.md
      cp src/site/pandoc/pandoc.css #{tmp}/
      cd #{tmp}
      git add -A
      git commit -am 'updating documentation'
      git push origin gh-pages
    EOS
  end
end
