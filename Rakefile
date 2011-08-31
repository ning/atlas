
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

namespace :ec2 do

  desc "kill all ec2 instances"
  task :kill do
    sh "ec2din | grep INSTANCE | cut -f 2 | xargs ec2kill"
    sh "rds-describe-db-instances | grep DBINSTANCE | grep available | awk '{print $2}' | xargs -I@ rds-delete-db-instance @ --skip-final-snapshot -f"
    sh "elb-describe-lbs --show-long --delimiter '|' | cut -f 2 -f 8 -d '|' | grep 'i-' | sed 's/|/ --instances /' | xargs elb-deregister-instances-from-lb"
  end

  desc "run tests which use ec2"
  task :test do
    sh "mvn -DRUN_EC2_TESTS=true clean test"
    Rake::Task['ec2:kill'].execute
  end

end


desc "clean up build cruft"
task :clean do
  sh "rm -rf target"
end


## Tasks and helper function for documentation generation
namespace :docs do

  desc "build docs, into target/site by default, or override with [<dir>]"
  task :build, [:dir] do |t, args|
    args.with_defaults(:dir => File.join("target", "site"))
    sh <<-EOS
      mkdir -p #{args.dir}
      pandoc --toc --html5 -f markdown -t html -c pandoc.css --template src/site/pandoc/template.html \
         -o #{args.dir}/index.html \
         src/site/pandoc/index.md \
         src/site/pandoc/building.md \
         src/site/pandoc/running.md \
         src/site/pandoc/configuring.md
      cp src/site/pandoc/pandoc.css #{args.dir}/
    EOS
  end

  desc "generate documenation and check it into gh-pages branch"
  task :github do
    require 'tmpdir'
    Dir.mktmpdir do |tmp|
      sh <<-EOS
        git fetch origin gh-pages
        if [ -z $(git branch | grep gh-pages) ]
          then
            git branch --track gh-pages origin/gh-pages
        fi
        git clone -b gh-pages . #{tmp}
      EOS
      Rake::Task["docs:build"].invoke(tmp)
      sh <<-EOS
        cd #{tmp}
        git add -A
        git commit -am 'updating documentation'
        git push origin gh-pages
      EOS
    end
  end
end
