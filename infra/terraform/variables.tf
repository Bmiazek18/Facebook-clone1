variable "proxmox_endpoint" {
  type        = string
  description = "Adres API Proxmoxa"
}

variable "proxmox_username" {
  type        = string
  description = "Użytkownik Proxmox"
  default     = "root@pam"
}

variable "proxmox_password" {
  type        = string
  description = "Hasło do Proxmoxa"
  sensitive   = true
}

variable "target_node" {
  type        = string
  description = "Nazwa węzła Proxmox"
  default     = "pve"
}

variable "container_vm_id" {
  type        = number
  description = "VM ID dla kontenera LXC z klastrem K3s"
  default     = 400
}

variable "container_hostname" {
  type        = string
  description = "Hostname kontenera LXC z klastrem K3s"
  default     = "k8s-platform-01"
}

variable "container_ip" {
  type        = string
  description = "Statyczny adres IP dla węzła K3s (z maską CIDR)"
  default     = "192.168.0.200/24"
}

variable "container_cores" {
  type        = number
  description = "Liczba rdzeni CPU przydzielonych do kontenera LXC"
  default     = 8
}

variable "container_memory" {
  type        = number
  description = "Pamięć RAM (MB) przydzielona do kontenera LXC"
  default     = 24576
}

variable "container_swap" {
  type        = number
  description = "Pamięć SWAP (MB) przydzielona do kontenera LXC"
  default     = 8192
}

variable "gateway_ip" {
  type        = string
  description = "Brama domyślna (IP routera)"
  default     = "192.168.0.1"
}

variable "debian_template" {
  type        = string
  description = "Nazwa pliku szablonu Debian 13 w Proxmox"
  default     = "local:vztmpl/debian-13-standard_13.1-2_amd64.tar.zst"
}

variable "container_root_password" {
  type        = string
  description = "Hasło roota ustawiane podczas inicjalizacji kontenera"
  sensitive   = true
}

variable "ssh_public_key" {
  type        = string
  description = "Ścieżka do klucza publicznego SSH"
  default     = "~/.ssh/id_rsa.pub"
}

variable "ssh_private_key" {
  type        = string
  description = "Ścieżka do klucza prywatnego SSH"
  default     = "~/.ssh/id_rsa"
}

variable "cloudflare_tunnel_token" {
  description = "Token tunelu Cloudflare używany przez cloudflared run --token"
  type        = string
  sensitive   = true
}

variable "github_repo_url" {
  description = "URL repozytorium Git używanego przez Argo CD"
  type        = string
  default     = "https://github.com/Bmiazek18/Facebook-clone1.git"
}

variable "github_repo_username" {
  description = "Login GitHub używany przez Argo CD do prywatnego repo"
  type        = string
}

variable "github_repo_password" {
  description = "Token GitHub/PAT używany przez Argo CD do prywatnego repo"
  type        = string
  sensitive   = true
}

variable "enable_gpu_passthrough" {
  description = "Włącz przekazywanie karty NVIDIA RTX 3070 do kontenera LXC i K3s"
  type        = bool
  default     = true
}

variable "proxmox_host_ip" {
  description = "Adres IP hosta Proxmox do konfiguracji GPU przez SSH (opcjonalny, domyślnie IP z endpointu)"
  type        = string
  default     = ""
}
