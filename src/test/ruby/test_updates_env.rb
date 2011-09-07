

environment "reinflation" do
  provisioner com.ning.atlas.noop.NoOpProvisioner

  installer "galaxy", com.ning.atlas.noop.NoOpInstaller
  initializer "chef", com.ning.atlas.noop.NoOpInstaller

  base "server", :init => ["chef:role[wombatypus]"]
end
