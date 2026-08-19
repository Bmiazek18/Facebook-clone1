# GitOps

Argo CD czyta ten katalog jako jedną aplikację nadrzędną i wdraża charty z `charts/`.

| Aplikacja | Chart | Publiczny hostname (Cloudflare Tunnel) | Serwis w klastrze |
| --- | --- | --- | --- |
| `facebook-frontend` | `charts/frontend` | `lab-bm.com` | `http://facebook-frontend-svc.apps.svc.cluster.local:3000` |
| `keycloak` | `charts/keycloak` | `auth.lab-bm.com` | `http://keycloak-service.apps.svc.cluster.local:8080` |
| `facebook-backend` | `charts/backend` | `api.lab-bm.com` | `http://apollo-router.apps.svc.cluster.local:4000` |

REST (`/api`) z `userservice` zostaje wewnętrzny. Jeśli będzie potrzebny publicznie, dodaj w tunelu path `/api*` na `http://userservice.apps.svc.cluster.local:8081`.
