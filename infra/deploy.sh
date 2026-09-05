#!/usr/bin/env bash
set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR/terraform"
ANSIBLE_DIR="$SCRIPT_DIR/ansible"

echo "================================================================="
echo "        🚀 Homelab Enterprise Infrastructure Deployer            "
echo "================================================================="

# 1. Walidacja zależności
command -v terraform >/dev/null 2>&1 || { echo "❌ Błąd: Terraform nie jest zainstalowany."; exit 1; }
command -v ansible-playbook >/dev/null 2>&1 || { echo "❌ Błąd: Ansible nie jest zainstalowane."; exit 1; }

# 2. Prowizjonowanie infrastruktury (Terraform)
echo ""
echo "📦 [1/3] Uruchamianie Terraform (Prowizjonowanie LXC / VM na Proxmox)..."
cd "$TERRAFORM_DIR"
terraform init
terraform apply -auto-approve

# 3. Odczytanie parametrów z Terraform
CONTAINER_IP=$(terraform output -raw container_ip 2>/dev/null || echo "192.168.0.200")
echo "✅ Węzeł utworzony z adresem IP: $CONTAINER_IP"

# 4. Oczekiwanie na gotowość SSH
echo ""
echo "⏳ [2/3] Oczekiwanie na dostępność portu SSH ($CONTAINER_IP:22)..."
for i in {1..30}; do
  if nc -z -w 2 "$CONTAINER_IP" 22 2>/dev/null; then
    echo "✅ Port SSH jest otwarty i odpowiada!"
    break
  fi
  echo "   ...oczekiwanie na SSH ($i/30)"
  sleep 2
done

# 5. Konfiguracja systemu OS, GPU i K3s (Ansible)
echo ""
echo "⚙️ [3/3] Uruchamianie Ansible Playbook..."
cd "$ANSIBLE_DIR"
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i inventory.ini playbook.yml

echo ""
echo "================================================================="
echo "  🎉 Sukces! Środowisko K3s, GPU i ArgoCD zostały skonfigurowane."
echo "================================================================="
