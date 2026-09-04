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
    size         = 60
  }

  operating_system {
    template_file_id = var.debian_template
    type             = "debian"
  }

  network_interface {
    name = "eth0"
  }

  connection {
    type        = "ssh"
    user        = "root"
    password    = var.container_root_password
    private_key = try(file(pathexpand(var.ssh_private_key)), null)
    host        = split("/", var.container_ip)[0]
  }

  provisioner "remote-exec" {
    inline = [
      "set -e",
      "echo '=== 1. Czekam na siec i instaluje zaleznosci ==='",
      "until systemctl is-active --quiet systemd-resolved || systemctl is-active --quiet networking; do sleep 2; done",
      "apt-get update -y && apt-get install -y curl git iptables ethtool wget gpg gnupg",
      "sysctl -w net.ipv4.ip_forward=1 || true",
      "iptables -P FORWARD ACCEPT",
      "echo '=== 2. Przygotowanie srodowiska LXC dla K3s ==='",
      "ln -sf /dev/null /dev/kmsg",
      "ln -sf /bin/true /sbin/modprobe",
      "mkdir -p /etc/rancher/k3s",
      "cat <<'EOF' > /etc/rancher/k3s/config.yaml",
      "disable:",
      "  - traefik",
      "write-kubeconfig-mode: \"0644\"",
      "flannel-backend: \"host-gw\"",
      "kubelet-arg:",
      "  - \"protect-kernel-defaults=false\"",
      "EOF",
      "echo '=== 3. Tworzenie obejscia dla read-only /proc/sys i kmsg w LXC ==='",
      "mkdir -p /etc/systemd/system/k3s.service.d",
      "cat <<'EOF' > /etc/systemd/system/k3s.service.d/lxc-fix.conf",
      "[Service]",
      "ExecStartPre=+/bin/mount -o remount,rw /proc/sys",
      "ExecStartPre=+/bin/sh -c 'ln -sf /dev/null /dev/kmsg'",
      "EOF",
      "systemctl daemon-reload || true",
      "echo '=== 4. Instalacja K3s Server ==='",
      "curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.28.9+k3s1 sh -",
      "ln -sf /usr/local/bin/k3s /usr/bin/k3s",
      "ln -sf /usr/local/bin/k3s /usr/bin/kubectl",
      "ethtool -K eth0 tx off rx off 2>/dev/null || true",
      "echo '=== 5. Oczekiwanie na gotowosc wezla K3s ==='",
      "export KUBECONFIG=/etc/rancher/k3s/k3s.yaml",
      "echo 'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml' >> /root/.bashrc",
      "until k3s kubectl get nodes 2>/dev/null | grep -q 'Ready'; do echo 'Inicjalizacja API Servera...'; sleep 3; done",
      "echo 'Klaster K3s jest w stanie Ready!'",
      "echo '=== 6. Tworzenie Namespaces ==='",
      "k3s kubectl create namespace apps --dry-run=client -o yaml | k3s kubectl apply -f -",
      "k3s kubectl create namespace infra --dry-run=client -o yaml | k3s kubectl apply -f -",
      "k3s kubectl create namespace argocd --dry-run=client -o yaml | k3s kubectl apply -f -",
      "echo '=== 7. Instalacja Argo CD ==='",
      "k3s kubectl apply --server-side -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml",
      "echo '=== 8. Udostepnienie interfejsu Argo CD (NodePort 30080 i 30443) ==='",
      "until k3s kubectl get svc argocd-server -n argocd >/dev/null 2>&1; do echo 'Czekam na serwer ArgoCD...'; sleep 2; done",
      "k3s kubectl patch svc argocd-server -n argocd -p '{\"spec\": {\"type\": \"NodePort\", \"ports\": [{\"name\": \"http\", \"port\": 80, \"targetPort\": 8080, \"nodePort\": 30080}, {\"name\": \"https\", \"port\": 443, \"targetPort\": 8083, \"nodePort\": 30443}]}}'",
      "echo '=== 9. Wdrozenie Cloudflare Tunnel ==='",
      "cat <<EOF | k3s kubectl apply -f -",
      "apiVersion: apps/v1",
      "kind: Deployment",
      "metadata:",
      "  name: cloudflared",
      "  namespace: infra",
      "spec:",
      "  replicas: 1",
      "  selector:",
      "    matchLabels:",
      "      app: cloudflared",
      "  template:",
      "    metadata:",
      "      labels:",
      "        app: cloudflared",
      "    spec:",
      "      containers:",
      "      - name: cloudflared",
      "        image: cloudflare/cloudflared:latest",
      "        args:",
      "        - tunnel",
      "        - --no-autoupdate",
      "        - run",
      "        - --token",
      "        - ${var.cloudflare_tunnel_token}",
      "        resources:",
      "          requests:",
      "            cpu: 50m",
      "            memory: 64Mi",
      "          limits:",
      "            cpu: 100m",
      "            memory: 128Mi",
      "EOF",
      "echo '=== 10. Konfiguracja dostępu do prywatnego repozytorium GitHub ==='",
      "k3s kubectl apply -f - <<EOF",
      "apiVersion: v1",
      "kind: Secret",
      "metadata:",
      "  name: repo-github-private",
      "  namespace: argocd",
      "  labels:",
      "    argocd.argoproj.io/secret-type: repository",
      "stringData:",
      "  type: git",
      "  url: ${var.github_repo_url}",
      "  username: ${var.github_repo_username}",
      "  password: ${var.github_repo_password}",
      "EOF",
      "echo '=== 11. Rejestracja aplikacji GitOps ==='",
      "until k3s kubectl get crd applications.argoproj.io >/dev/null 2>&1; do echo 'Czekam na CRD Argo CD...'; sleep 3; done",
      "k3s kubectl apply -f - <<EOF",
      "apiVersion: argoproj.io/v1alpha1",
      "kind: Application",
      "metadata:",
      "  name: platform-apps",
      "  namespace: argocd",
      "  finalizers:",
      "    - resources-finalizer.argocd.argoproj.io",
      "spec:",
      "  project: default",
      "  source:",
      "    repoURL: '${var.github_repo_url}'",
      "    targetRevision: HEAD",
      "    path: 'infra/argocd'",
      "  destination:",
      "    server: 'https://kubernetes.default.svc'",
      "    namespace: 'argocd'",
      "  syncPolicy:",
      "    automated:",
      "      prune: true",
      "      selfHeal: true",
      "EOF",
      "echo '=== Konfiguracja bazowa LXC zakonczona ==='"
    ]
  }
}

resource "null_resource" "proxmox_host_gpu_passthrough" {
  count = var.enable_gpu_passthrough ? 1 : 0

  depends_on = [proxmox_virtual_environment_container.k3s_cluster]

  connection {
    type        = "ssh"
    user        = "root"
    password    = var.proxmox_password
    private_key = try(file(pathexpand(var.ssh_private_key)), null)
    host        = var.proxmox_host_ip != "" ? var.proxmox_host_ip : regex("https?://([^:/]+)", var.proxmox_endpoint)[0]
  }

  provisioner "remote-exec" {
    inline = [
      "set -e",
      "echo '=== [1/3] Weryfikacja GPU na hoście Proxmox i konfiguracja reguł udev ==='",
      "nvidia-smi",
      "mkdir -p /etc/udev/rules.d",
      "cat <<'EOF_UDEV' > /etc/udev/rules.d/70-nvidia.rules",
      "KERNEL==\"nvidia*\", MODE=\"0666\"",
      "KERNEL==\"nvidia-uvm*\", MODE=\"0666\"",
      "EOF_UDEV",
      "udevadm control --reload-rules || true",
      "udevadm trigger || true",
      "echo '=== [2/3] Konfiguracja cgroup i mapowania GPU w /etc/pve/lxc/${var.container_vm_id}.conf ==='",
      "CONF_FILE='/etc/pve/lxc/${var.container_vm_id}.conf'",
      "if ! grep -q 'NVIDIA GPU Passthrough' \"$CONF_FILE\"; then",
      "  cat <<'EOF' >> \"$CONF_FILE\"",
      "# NVIDIA GPU Passthrough",
      "lxc.cgroup2.devices.allow: c 195:* rwm",
      "lxc.cgroup2.devices.allow: c 234:* rwm",
      "lxc.cgroup2.devices.allow: c 508:* rwm",
      "lxc.cgroup2.devices.allow: c 10:200 rwm",
      "lxc.mount.entry: /dev/nvidia0 dev/nvidia0 none bind,optional,create=file",
      "lxc.mount.entry: /dev/nvidiactl dev/nvidiactl none bind,optional,create=file",
      "lxc.mount.entry: /dev/nvidia-uvm dev/nvidia-uvm none bind,optional,create=file",
      "lxc.mount.entry: /dev/nvidia-uvm-tools dev/nvidia-uvm-tools none bind,optional,create=file",
      "lxc.mount.entry: /dev/nvidia-modeset dev/nvidia-modeset none bind,optional,create=file",
      "EOF",
      "fi",
      "echo 'Restart kontenera w celu podpięcia urządzeń GPU...'",
      "pct stop ${var.container_vm_id} || true",
      "sleep 3",
      "pct start ${var.container_vm_id} || true",
      "until pct exec ${var.container_vm_id} -- /usr/local/bin/k3s kubectl get nodes 2>/dev/null | grep -q 'Ready'; do echo 'API K3s wstaje...'; sleep 3; done",
      "echo '=== [3/3] Dopasowanie wersji bibliotek NVIDIA oraz instalacja NVIDIA Container Toolkit wewnątrz kontenera LXC ==='",
      "DRIVER_VER=$(nvidia-smi --query-gpu=driver_version --format=csv,noheader | head -n1 | tr -d '[:space:]')",
      "echo \"Wykryto wersje sterownika hosta: $DRIVER_VER\"",
      "RUN_FILE=\"/tmp/NVIDIA-Linux-x86_64-$DRIVER_VER.run\"",
      "if [ ! -f \"$RUN_FILE\" ]; then",
      "  echo 'Pobieranie instalatora NVIDIA dla kontenera...'",
      "  wget -q \"https://us.download.nvidia.com/XFree86/Linux-x86_64/$DRIVER_VER/NVIDIA-Linux-x86_64-$DRIVER_VER.run\" -O \"$RUN_FILE\" || true",
      "fi",
      "pct push ${var.container_vm_id} \"$RUN_FILE\" /tmp/nvidia-installer.run",
      "pct exec ${var.container_vm_id} -- bash -c \"DEBIAN_FRONTEND=noninteractive apt-get purge -y 'nvidia*' 'libnvidia*' 'glx-alternative-*' 2>/dev/null || true; chmod +x /tmp/nvidia-installer.run; /tmp/nvidia-installer.run --no-kernel-module -s --no-questions; rm -f /tmp/nvidia-installer.run; ldconfig; ln -sf /usr/local/bin/k3s /usr/bin/k3s || true; ln -sf /usr/local/bin/k3s /usr/bin/kubectl || true\"",
      "echo 'Instalacja nvidia-container-toolkit i konfiguracja containerd...'",
      "pct exec ${var.container_vm_id} -- bash -c \"rm -f /etc/apt/sources.list.d/nvidia* /usr/share/keyrings/nvidia* /etc/apt/trusted.gpg.d/nvidia*; apt-get update -y; DEBIAN_FRONTEND=noninteractive apt-get install -y gpg gnupg curl; mkdir -p /usr/share/keyrings /etc/cdi; curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg; echo 'deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://nvidia.github.io/libnvidia-container/stable/deb/amd64 /' > /etc/apt/sources.list.d/nvidia-container-toolkit.list; apt-get update -y; DEBIAN_FRONTEND=noninteractive apt-get install -y nvidia-container-toolkit; nvidia-ctk cdi generate --output=/etc/cdi/nvidia.yaml; nvidia-ctk runtime configure --runtime=containerd; systemctl restart k3s\"",
      "echo 'Weryfikacja nvidia-smi oraz nvidia-ctk wewnątrz kontenera LXC:'",
      "pct exec ${var.container_vm_id} -- nvidia-smi",
      "pct exec ${var.container_vm_id} -- nvidia-ctk --version",
      "echo '=== Passthrough GPU, biblioteki LXC oraz NVIDIA Container Toolkit skonfigurowane pomyślnie! ==='"
    ]
  }
}