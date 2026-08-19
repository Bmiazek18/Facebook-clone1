# Infra

- `infra/terraform` — klaster K3s, Argo CD, Cloudflare Tunnel
- `infra/argocd` — aplikacje GitOps dla `charts/frontend`, `charts/keycloak` i `charts/backend`

API jest dostępne przez subdomenę tunelu, np. `api.lab-bm.com` → `apollo-router:4000`. Ingress nie jest używany.

## CloudNativePG - High Availability PostgreSQL

Konfiguracja PostgreSQL z automatycznym failover, replication i backupami jest w `charts/backend/templates/cloudnative-pg.yaml`.

### Instalacja

```bash
# 1. Zainstaluj CloudNativePG operator
helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo update
helm install cnpg cnpg/cloudnative-pg --namespace cnpg-system --create-namespace

# 2. Postgres Cluster utworzy się automatycznie z ArgoCD (3 replicas z HA)
# Serwisy:
# - postgresql.apps.svc.cluster.local:5432 (loadbalanced - read/write)
# - postgresql-rw.apps.svc.cluster.local:5432 (primary - write only)
# - postgresql-ro.apps.svc.cluster.local:5432 (replicas - read only)
```

### Failover

Automatyczny failover na jedną z 3 replik w mniej niż 30 sekund. Nie trzeba nic robić!
