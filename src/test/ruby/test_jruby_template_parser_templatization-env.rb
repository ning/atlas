environment "breakfast" do

  base "mysql", {
      :provisioner => ["rds", {
        :name           => '{base.fragment}',
        :storage_size   => '5',
        :instance_class => "db.m1.small",
        :engine         => "MySQL",
        :username       => "wp",
        :password       => "wp"
    }]
  }
end