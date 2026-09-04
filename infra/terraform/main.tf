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
      "echo '=== 12. Konfiguracja NVIDIA GPU Time-Slicing (4 vGPU) & Device Plugin dla K3s ==='",
      "if [ \"${var.enable_gpu_passthrough}\" = \"true\" ]; then",
      "  sed -i -E 's/(main|main contrib)$/\\1 contrib non-free non-free-firmware/' /etc/apt/sources.list || true",
      "  apt-get update -y && DEBIAN_FRONTEND=noninteractive apt-get install -y nvidia-smi nvidia-driver-libs || true",
      "  k3s kubectl apply -f - <<'EOF_GPU'",
      "apiVersion: v1",
      "kind: ConfigMap",
      "metadata:",
      "  name: nvidia-device-plugin-config",
      "  namespace: kube-system",
      "data:",
      "  config.yaml: |",
      "    version: v1",
      "    sharing:",
      "      timeSlicing:",
      "        resources:",
      "          - name: nvidia.com/gpu",
      "            replicas: 4",
      "EOF_GPU",
      "  k3s kubectl apply -f - <<'EOF_DS'",
      "apiVersion: apps/v1",
      "kind: DaemonSet",
      "metadata:",
      "  name: nvidia-device-plugin-daemonset",
      "  namespace: kube-system",
      "spec:",
      "  selector:",
      "    matchLabels:",
      "      name: nvidia-device-plugin-ds",
      "  template:",
      "    metadata:",
      "      labels:",
      "        name: nvidia-device-plugin-ds",
      "    spec:",
      "      tolerations:",
      "      - key: CriticalAddonsOnly",
      "        operator: Exists",
      "      - key: nvidia.com/gpu",
      "        operator: Exists",
      "        effect: NoSchedule",
      "      containers:",
      "      - image: nvcr.io/nvidia/k8s-device-plugin:v0.15.0",
      "        name: nvidia-device-plugin-ctr",
      "        env:",
      "          - name: CONFIG_FILE",
      "            value: /etc/config/config.yaml",
      "        securityContext:",
      "          privileged: true",
      "        volumeMounts:",
      "        - name: device-plugin",
      "          mountPath: /var/lib/kubelet/device-plugins",
      "        - name: config",
      "          mountPath: /etc/config",
      "        - name: dev",
      "          mountPath: /dev",
      "      volumes:",
      "      - name: device-plugin",
      "        hostPath:",
      "          path: /var/lib/kubelet/device-plugins",
      "      - name: config",
      "        configMap:",
      "          name: nvidia-device-plugin-config",
      "      - name: dev",
      "        hostPath:",
      "          path: /dev",
      "EOF_DS",
      "  echo '=== Wdrażanie NVIDIA DCGM Exporter (Metryki GPU dla Prometheus i Grafana) ==='",
      "  k3s kubectl apply -f - <<'EOF_DCGM'",
      "apiVersion: apps/v1",
      "kind: DaemonSet",
      "metadata:",
      "  name: nvidia-dcgm-exporter",
      "  namespace: kube-system",
      "  labels:",
      "    app.kubernetes.io/name: nvidia-dcgm-exporter",
      "spec:",
      "  selector:",
      "    matchLabels:",
      "      app.kubernetes.io/name: nvidia-dcgm-exporter",
      "  template:",
      "    metadata:",
      "      labels:",
      "        app.kubernetes.io/name: nvidia-dcgm-exporter",
      "      annotations:",
      "        prometheus.io/scrape: \"true\"",
      "        prometheus.io/port: \"9400\"",
      "        prometheus.io/path: \"/metrics\"",
      "    spec:",
      "      tolerations:",
      "      - key: CriticalAddonsOnly",
      "        operator: Exists",
      "      - key: nvidia.com/gpu",
      "        operator: Exists",
      "        effect: NoSchedule",
      "      containers:",
      "      - name: nvidia-dcgm-exporter",
      "        image: nvcr.io/nvidia/k8s/dcgm-exporter:3.3.5-3.4.0-ubuntu22.04",
      "        securityContext:",
      "          privileged: true",
      "        ports:",
      "        - name: metrics",
      "          containerPort: 9400",
      "        env:",
      "        - name: DCGM_EXPORTER_LISTEN",
      "          value: \":9400\"",
      "---",
      "apiVersion: v1",
      "kind: Service",
      "metadata:",
      "  name: nvidia-dcgm-exporter",
      "  namespace: kube-system",
      "  labels:",
      "    app.kubernetes.io/name: nvidia-dcgm-exporter",
      "  annotations:",
      "    prometheus.io/scrape: \"true\"",
      "    prometheus.io/port: \"9400\"",
      "spec:",
      "  type: ClusterIP",
      "  ports:",
      "  - name: metrics",
      "    port: 9400",
      "    targetPort: 9400",
      "  selector:",
      "    app.kubernetes.io/name: nvidia-dcgm-exporter",
      "EOF_DCGM",
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
    password    = var.proxmox_password
    private_key = try(file(pathexpand(var.ssh_private_key)), null)
    host        = var.proxmox_host_ip != "" ? var.proxmox_host_ip : regex("https?://([^:/]+)", var.proxmox_endpoint)[0]
  }

  provisioner "remote-exec" {
    inline = [
      "set -e",
      "echo '=== [1/5] Konfiguracja repozytoriow APT (PVE No-Subscription + non-free) na Proxmox VE ==='",
      "rm -f /etc/apt/sources.list.d/pve-enterprise.list /etc/apt/sources.list.d/ceph.list 2>/dev/null || true",
      "if [ -f /etc/apt/sources.list.d/debian.sources ]; then",
      "  sed -i 's/Components: main$/Components: main contrib non-free non-free-firmware/g' /etc/apt/sources.list.d/debian.sources",
      "  sed -i 's/Components: main contrib$/Components: main contrib non-free non-free-firmware/g' /etc/apt/sources.list.d/debian.sources",
      "fi",
      "if [ -f /etc/apt/sources.list ]; then",
      "  sed -i -E 's/(main|main contrib)$/\\1 contrib non-free non-free-firmware/' /etc/apt/sources.list || true",
      "fi",
      "for f in /etc/apt/sources.list.d/*.list; do [ -f \"$f\" ] && sed -i -E 's/(main|main contrib)$/\\1 contrib non-free non-free-firmware/' \"$f\" || true; done",
      "SUITE=$(grep -oP '(?<=VERSION_CODENAME=)[a-z]+' /etc/os-release 2>/dev/null || echo 'bookworm')",
      "cat <<EOF_PVE > /etc/apt/sources.list.d/pve-no-subscription.list",
      "deb http://download.proxmox.com/debian/pve $SUITE pve-no-subscription",
      "EOF_PVE",
      "apt-get update -y || true",
      "echo '=== [2/5] Instalacja naglowkow kernela i sterownikow NVIDIA na Proxmox ==='",
      "if ! which nvidia-smi >/dev/null 2>&1 || ! lsmod | grep -q nvidia; then",
      "  echo 'Instaluje naglowki kernela, dkms i build-essential...'",
      "  DEBIAN_FRONTEND=noninteractive apt-get install -y pve-headers-$(uname -r) proxmox-headers-$(uname -r) proxmox-default-headers pve-headers linux-headers-$(uname -r) dkms build-essential || true",
      "  echo 'Instaluje sterownik nvidia-driver...'",
      "  DEBIAN_FRONTEND=noninteractive apt-get install -y nvidia-driver nvidia-smi nvidia-kernel-dkms firmware-misc-nonfree || true",
      "fi",
      "echo 'Ladowanie modulow NVIDIA...'",
      "modprobe nvidia || true",
      "modprobe nvidia_uvm || true",
      "modprobe nvidia_modeset || true",
      "mkdir -p /etc/modules-load.d",
      "cat <<'EOF_MOD' > /etc/modules-load.d/nvidia.conf",
      "nvidia",
      "nvidia-uvm",
      "nvidia-modeset",
      "EOF_MOD",
      "cat <<'EOF_UDEV' > /etc/udev/rules.d/70-nvidia.rules",
      "KERNEL==\"nvidia*\", MODE=\"0666\"",
      "KERNEL==\"nvidia-uvm*\", MODE=\"0666\"",
      "EOF_UDEV",
      "udevadm control --reload-rules || true",
      "udevadm trigger || true",
      "nvidia-smi || true",
      "echo '=== [3/5] Dopisywanie regul GPU do konfiguracji kontenera LXC ${var.container_vm_id} ==='",
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
      "echo '=== [4/5] Restart kontenera ${var.container_vm_id} w celu podpiecia urzadzen GPU ==='",
      "pct reboot ${var.container_vm_id} || pct start ${var.container_vm_id} || true",
      "echo 'Czekam na uruchomienie kontenera...'",
      "until pct status ${var.container_vm_id} | grep -q 'running'; do sleep 2; done",
      "sleep 5",
      "echo '=== [5/5] Inicjalizacja bibliotek NVIDIA i przeladowanie K8s Device Plugin w kontenerze ==='",
      "pct exec ${var.container_vm_id} -- bash -c \"export PATH=\\$PATH:/usr/local/bin:/usr/sbin:/sbin; if [ -f /etc/apt/sources.list.d/debian.sources ]; then sed -i 's/Components: main$/Components: main contrib non-free non-free-firmware/g' /etc/apt/sources.list.d/debian.sources; sed -i 's/Components: main contrib$/Components: main contrib non-free non-free-firmware/g' /etc/apt/sources.list.d/debian.sources; fi; if [ -f /etc/apt/sources.list ]; then sed -i -E 's/(main|main contrib)$/\\1 contrib non-free non-free-firmware/' /etc/apt/sources.list; fi; apt-get update -y || true; DEBIAN_FRONTEND=noninteractive apt-get install -y nvidia-smi nvidia-driver-libs || true; /usr/local/bin/k3s kubectl rollout restart daemonset nvidia-device-plugin-daemonset -n kube-system || true; /usr/local/bin/k3s kubectl rollout restart daemonset nvidia-dcgm-exporter -n kube-system || true\"",
      "echo '=== Pelny NVIDIA GPU Passthrough i konfiguracja K8s zakonczone sukcesem! ==='"
    ]
  }
}
