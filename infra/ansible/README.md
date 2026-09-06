# Platform Ansible Automation

Ten katalog odpowiada za idempotentną konfigurację systemów operacyjnych, passthrough GPU NVIDIA, klastra K3s oraz wdrożenie Argo CD i GitOps w oparciu o modułowe **Ansible Roles**.

## Struktura

```
infra/ansible/
├── ansible.cfg            # Konfiguracja Ansible (timeouty, pipelining)
├── group_vars/
│   └── all.yml            # Domyślne zmienne globalne
├── roles/
│   ├── proxmox_gpu/       # Konfiguracja udev i passthrough GPU na hoście Proxmox
│   ├── system_common/     # Pakiety bazowe, sysctl, symlinki dla kontenera LXC
│   ├── k3s_server/        # Instalacja K3s, drop-in systemd, flannel host-gw
│   ├── nvidia_container/  # NVIDIA Container Toolkit, CDI specs, containerd runtime
│   └── argocd_gitops/     # Namespaces, Argo CD, Cloudflare Tunnel, root app
├── inventory.ini          # Plik inwentarza hostów (generowany z Terraform lub ręczny)
├── inventory.ini.example  # Wzór pliku inwentarza
├── playbook.yml           # Główny playbook orkiestrujący role
└── README.md
```

## Szybki start

1. **Uzupełnij inwentarz `inventory.ini`:**
   ```ini
   [proxmox_host]
   pve_node ansible_host=192.168.0.200 ansible_user=root

   [k3s_node]
   k8s_node ansible_host=192.168.0.210 ansible_user=root
   ```

2. **Uruchom pełny playbook:**
   ```bash
   ansible-playbook -i inventory.ini playbook.yml
   ```

3. **Uruchamianie z wybranymi tagami / rolami:**
   ```bash
   # Tylko konfiguracja GPU (Proxmox host + LXC):
   ansible-playbook -i inventory.ini playbook.yml --tags "gpu_host,gpu_container"

   # Tylko instalacja K3s:
   ansible-playbook -i inventory.ini playbook.yml --tags "k3s"

   # Tylko konfiguracja GitOps i Argo CD:
   ansible-playbook -i inventory.ini playbook.yml --tags "gitops,argocd"
   ```

## Przekazywanie sekretów

Tokeny Cloudflare oraz dane dostępowe do prywatnego repozytorium GitHub możesz przekazać:
- Przez flagę `-e`:
  ```bash
  ansible-playbook -i inventory.ini playbook.yml \
    -e "cloudflare_tunnel_token=TWOJ_TOKEN" \
    -e "github_repo_username=TWOJ_LOGIN" \
    -e "github_repo_password=TWOJ_PAT"
  ```
- Albo zaszyfrować w pliku za pomocą `ansible-vault`.
