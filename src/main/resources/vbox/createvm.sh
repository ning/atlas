#!/bin/sh

# ----------
# This script will create the recommended bare hardware
# IMPORTANT: You should manually create the disk and mount the installer iso after running this script
# IMPORTANT: Configure the following before running this script
# ----------

VMNAME="atlas-natty32"
BRIDGE_ONLY_ADAPTER="en1: AirPort" # To findout, run $ VBoxManage list bridgedifs
INTNET_ADAPTER="atlas-intnet"

# ----------

VBoxManage createvm --name ${VMNAME} --ostype Ubuntu --register

VBoxManage modifyvm ${VMNAME} --memory 384 --vram 24 --pae on --usb off --vrde off --audio none
VBoxManage modifyvm ${VMNAME} --nic1 bridged --nictype1 82540EM --bridgeadapter1 "${BRIDGE_ONLY_ADAPTER}" --nicpromisc1 "allow-all"
VBoxManage modifyvm ${VMNAME} --nic2 intnet --nictype2 82540EM --intnet2 ${INTNET_ADAPTER} --nicpromisc2 "allow-vms"


