resource "proxmox_virtual_environment_container" "k3s_cluster" {
  node_name = var.target_node
  vm_id     = var.container_vm_id

  unprivileged = false

  features {
    nesting = true
    keyctl  = true
  }

  initialization {
    hostname = var.container_hostname

    ip_config {
      ipv4 {
        address = var.container_ip
        gateway = var.gateway_ip
      }
    }

    user_account {
      password = var.container_root_password
      keys = [
        trimspace(file(pathexpand(var.ssh_public_key)))
      ]
    }
  }

  cpu {
    cores = var.container_cores
  }

  memory {
    dedicated = var.container_memory
    swap      = var.container_swap
  }

  disk {
    datastore_id = "local-lvm"
    size         = 100
  }

  operating_system {
    template_file_id = var.debian_template
    type             = "debian"
  }

  network_interface {
    name = "eth0"
  }
}

# Automatyczne wygenerowanie inventory dla Ansible po zakończeniu provisioning'u kontenera
resource "local_file" "ansible_inventory" {
  filename = "${path.module}/../ansible/inventory.ini"
  content  = <<-EOT
[proxmox_host]
pve_node ansible_host=${var.proxmox_host_ip != "" ? var.proxmox_host_ip : regex("https?://([^:/]+)", var.proxmox_endpoint)[0]} ansible_user=${var.proxmox_username != "" ? split("@", var.proxmox_username)[0] : "root"}

[k3s_node]
k8s_node ansible_host=${split("/", var.container_ip)[0]} ansible_user=root
EOT
}