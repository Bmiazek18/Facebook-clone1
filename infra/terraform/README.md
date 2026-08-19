# Platform Terraform

Ten katalog przygotowuje klaster. Aplikacje żyją w Helm chartach i Argo CD.

## Co tworzy

- kontener LXC na Proxmoxie pod klaster K3s
- namespace'y `apps`, `infra` i `argocd`
- Argo CD
- `cloudflared` (Cloudflare Tunnel)
- sekret repozytorium GitHub
- aplikację nadrzędną `platform-apps`, która wdraża `charts/frontend`, `charts/keycloak` i `charts/backend`

## Cloudflare Tunnel

Publiczny ruch nie idzie przez Ingress. W Zero Trust dodaj hostname'y do istniejącego tunelu:

| Hostname | Serwis w klastrze |
| --- | --- |
| `lab-bm.com` | `http://facebook-frontend-svc.apps.svc.cluster.local:3000` |
| `auth.lab-bm.com` | `http://keycloak-service.apps.svc.cluster.local:8080` |
| `api.lab-bm.com` | `http://apollo-router.apps.svc.cluster.local:4000` |

Opcjonalnie REST: path `/api*` na `http://userservice.apps.svc.cluster.local:8081`.

## Bezpieczne użycie

Nie commituj lokalnych plików stanu ani sekretów:

```bash
cp terraform.tfvars.example terraform.tfvars
```
