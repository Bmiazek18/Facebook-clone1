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
    cores = 4
  }

  memory {
    dedicated = 8192
    swap      = 2048
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
    private_key = file(pathexpand(var.ssh_private_key))
    host        = split("/", var.container_ip)[0]
  }

  provisioner "remote-exec" {
    inline = [
      "set -e",
      "echo '=== 1. Czekam na siec i instaluje zaleznosci ==='",
      "until systemctl is-active --quiet systemd-resolved || systemctl is-active --quiet networking; do sleep 2; done",
      "apt-get update -y && apt-get install -y curl git iptables ethtool",
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
      "echo '=== 8. Udostepnienie interfejsu Argo CD (NodePort 30080) ==='",
      "until k3s kubectl get svc argocd-server -n argocd >/dev/null 2>&1; do echo 'Czekam na utworzenie serwisu argocd-server...'; sleep 2; done",
      "k3s kubectl patch svc argocd-server -n argocd -p '{\"spec\": {\"type\": \"NodePort\", \"ports\": [{\"name\": \"http\", \"port\": 80, \"targetPort\": 8080, \"nodePort\": 30080}, {\"name\": \"https\", \"port\": 443, \"targetPort\": 8080, \"nodePort\": 30443}]}}'",
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
      "echo '=== 11. Rejestracja aplikacji GitOps (frontend, keycloak, backend) ==='",
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
      "echo '=== 12. Konfiguracja NVIDIA GPU & Device Plugin dla K3s ==='",
      "if [ \"${var.enable_gpu_passthrough}\" = \"true\" ]; then",
      "  apt-get update -y && (apt-get install -y nvidia-utils-535 || apt-get install -y nvidia-utils-550 || apt-get install -y nvidia-utils-headless-535 || true)",
      "  k3s kubectl apply -f https://raw.githubusercontent.com/NVIDIA/k8s-device-plugin/v0.14.5/deployments/static/gpu-operator/k8s-device-plugin.yaml",
      "fi",
      "echo '=== Pelna inicjalizacja i automatyzacja zakonczona sukcesem! ==='",
    ]
  }
}

resource "null_resource" "proxmox_host_gpu_passthrough" {
  count = var.enable_gpu_passthrough ? 1 : 0

  depends_on = [proxmox_virtual_environment_container.k3s_cluster]

  connection {
    type        = "ssh"
    user        = "root"
    private_key = file(pathexpand(var.ssh_private_key))
    host        = var.proxmox_host_ip != "" ? var.proxmox_host_ip : regex("https?://([^:/]+)", var.proxmox_endpoint)[0]
  }

  provisioner "remote-exec" {
    inline = [
      "set -e",
      "echo '=== Konfiguracja NVIDIA Passthrough na hoście Proxmox ==='",
      "modprobe nvidia || true",
      "modprobe nvidia_uvm || true",
      "modprobe nvidia_modeset || true",
      "mkdir -p /etc/modules-load.d",
      "grep -q 'nvidia-uvm' /etc/modules-load.d/nvidia.conf 2>/dev/null || echo -e 'nvidia\\nnvidia-uvm\\nnvidia-modeset' >> /etc/modules-load.d/nvidia.conf",
      "CONF_FILE='/etc/pve/lxc/${var.container_vm_id}.conf'",
      "if ! grep -q 'NVIDIA GPU Passthrough' \"$CONF_FILE\"; then",
      "  echo 'Dopisywanie regul GPU do konfiguracji kontenera ${var.container_vm_id}...'",
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
      "EOF",
      "  echo 'Restartowanie kontenera ${var.container_vm_id} w celu zaaplikowania GPU...'",
      "  pct reboot ${var.container_vm_id} || true",
      "fi",
      "echo 'NVIDIA GPU Passthrough zostal skonfigurowany pomyslnie na Proxmoxie!'"
    ]
  }
}
