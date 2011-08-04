#!/bin/sh

# ----------
# This script will create the recommended bare hardware
# Configure the following before running this script
# ----------

VMNAME="atlas-lucid32"
HOST_ONLY_ADAPTER="vboxnet0"
BRIDGE_ONLY_ADAPTER="en0: Ethernet"

# ----------

VBoxManage createvm --name ${VMNAME} --ostype Ubuntu --register

VBoxManage modifyvm ${VMNAME} --memory 384 --vram 24 --pae on --usb off --vrde off --audio none
VBoxManage modifyvm ${VMNAME} --nic1 hostonly --nictype1 82540EM --hostonlyadapter1 ${HOST_ONLY_ADAPTER} --nicpromisc1 "allow-vms"
VBoxManage modifyvm ${VMNAME} --nic2 bridged --nictype2 82540EM --bridgeadapter2 "${BRIDGE_ONLY_ADAPTER}" --nicpromisc2 "allow-all"
VBoxManage modifyvm ${VMNAME} --nic3 nat --nictype3 82540EM




