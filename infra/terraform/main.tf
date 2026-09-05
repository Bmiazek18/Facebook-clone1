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

  startup {
    order      = "1"
    up_delay   = "10"
    down_delay = "10"
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

# Opcjonalne uruchomienie Ansible Playbook bezpośrednio z Terraform (gdy auto_run_ansible = true)
resource "null_resource" "run_ansible" {
  count = var.auto_run_ansible ? 1 : 0

  depends_on = [
    proxmox_virtual_environment_container.k3s_cluster,
    local_file.ansible_inventory
  ]

  triggers = {
    container_id   = proxmox_virtual_environment_container.k3s_cluster.id
    inventory_hash = local_file.ansible_inventory.content_md5
  }

  provisioner "local-exec" {
    command = <<-EOT
      echo "=== [1/2] Oczekiwanie na dostępność SSH na węźle ${split("/", var.container_ip)[0]} ==="
      HOST="${split("/", var.container_ip)[0]}"
      for i in {1..30}; do
        if nc -z -w 2 "$HOST" 22 2>/dev/null; then
          echo "SSH jest gotowe na $HOST:22"
          break
        fi
        echo "Czekam na SSH ($i/30)..."
        sleep 2
      done

      echo "=== [2/2] Automatyczne uruchamianie Ansible Playbook ==="
      cd "${path.module}/../ansible" && ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i inventory.ini playbook.yml \
        -e "cloudflare_tunnel_token=${var.cloudflare_tunnel_token}" \
        -e "github_repo_username=${var.github_repo_username}" \
        -e "github_repo_password=${var.github_repo_password}" \
        -e "github_repo_url=${var.github_repo_url}"
    EOT
  }
}