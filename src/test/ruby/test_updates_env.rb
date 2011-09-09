

environment "reinflation" do
  provisioner "noop", com.ning.atlas.noop.NoOpProvisioner

  installer "galaxy", com.ning.atlas.noop.NoOpInstaller
  initializer "chef", com.ning.atlas.noop.NoOpInstaller

  base "server", provisioner: "noop", init: ["chef:role[wombatypus]"]
end
