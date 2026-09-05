output "container_vm_id" {
  description = "ID utworzonego kontenera LXC"
  value       = proxmox_virtual_environment_container.k3s_cluster.vm_id
}

output "container_ip" {
  description = "Adres IP węzła K3s"
  value       = split("/", var.container_ip)[0]
}

output "proxmox_host_ip" {
  description = "Adres IP hosta Proxmox"
  value       = var.proxmox_host_ip != "" ? var.proxmox_host_ip : regex("https?://([^:/]+)", var.proxmox_endpoint)[0]
}

output "ansible_command" {
  description = "Polecenie uruchomienia Ansible po zakończeniu provisioning'u Terraform"
  value       = "cd ../ansible && ansible-playbook playbook.yml"
}
