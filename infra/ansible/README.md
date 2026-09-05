# Platform Ansible Automation

Ten katalog odpowiada za idempotentną konfigurację systemów operacyjnych, passthrough GPU NVIDIA, klastra K3s oraz wdrożenie Argo CD i GitOps.

## Struktura

```
infra/ansible/
├── ansible.cfg            # Konfiguracja Ansible (timeouty, pipelining)
├── group_vars/
│   └── all.yml            # Domyślne zmienne (wersje, konfiguracja)
├── inventory.ini          # Plik inwentarza hostów (generowany z Terraform lub ręczny)
├── inventory.ini.example  # Wzór pliku inwentarza
├── playbook.yml           # Główny playbook (Play 1: Proxmox Host, Play 2: K3s Node)
└── README.md
```

## Szybki start

1. **Uzupełnij inwentarz `inventory.ini`:**
   ```ini
   [proxmox_host]
   pve_node ansible_host=192.168.0.100 ansible_user=root

   [k3s_node]
   k8s_node ansible_host=192.168.0.200 ansible_user=root
   ```

2. **Uruchom pełny playbook:**
   ```bash
   ansible-playbook playbook.yml
   ```

3. **Uruchamianie z wybranymi tagami (np. tylko GPU lub tylko K3s):**
   ```bash
   # Tylko konfiguracja GPU na hoście Proxmox i w kontenerze:
   ansible-playbook playbook.yml --tags "gpu_host,gpu_container"

   # Tylko konfiguracja węzła K3s i GitOps:
   ansible-playbook playbook.yml --tags "k3s"
   ```

## Przekazywanie sekretów

Tokeny Cloudflare oraz dane dostępowe do prywatnego repozytorium GitHub możesz przekazać:
- Przez flagę `-e`:
  ```bash
  ansible-playbook playbook.yml \
    -e "cloudflare_tunnel_token=TWOJ_TOKEN" \
    -e "github_repo_username=TWOJ_LOGIN" \
    -e "github_repo_password=TWOJ_PAT"
  ```
- Albo zaszyfrować w pliku za pomocą `ansible-vault`.
