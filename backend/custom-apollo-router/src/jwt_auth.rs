use apollo_router::layers::ServiceBuilderExt;
use apollo_router::plugin::{Plugin, PluginInit};
use apollo_router::services::router;
use jsonwebtoken::{decode, decode_header, Algorithm, DecodingKey, Validation};
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;
use tower::{BoxError, ServiceBuilder, ServiceExt};

#[derive(Debug, Deserialize, JsonSchema)]
pub struct Config {
    pub jwks_url: String,
    pub issuer: String,
}

#[derive(Debug, Serialize, Deserialize)]
struct Claims {
    pub sub: Option<String>,
    pub exp: Option<usize>,
    pub iss: Option<String>,
    pub entity_type: Option<String>,
    pub page_id: Option<String>,
}

#[derive(Clone)]
pub struct JwtAuthPlugin {
    config: Arc<Config>,
    keys: Arc<RwLock<HashMap<String, DecodingKey>>>,
}

#[async_trait::async_trait]
impl Plugin for JwtAuthPlugin {
    type Config = Config;

    async fn new(init: PluginInit<Self::Config>) -> Result<Self, BoxError> {
        let config = Arc::new(init.config);
        let keys = Arc::new(RwLock::new(HashMap::new()));

        let c = config.clone();
        let k = keys.clone();
        tokio::spawn(async move {
            loop {
                if let Ok(resp) = reqwest::get(&c.jwks_url).await {
                    if let Ok(jwks) = resp.json::<serde_json::Value>().await {
                        if let Some(keys_arr) = jwks.get("keys").and_then(|k| k.as_array()) {
                            let mut map = HashMap::new();
                            for key_val in keys_arr {
                                if let (Some(kid), Some(n), Some(e)) = (
                                    key_val.get("kid").and_then(|k| k.as_str()),
                                    key_val.get("n").and_then(|n| n.as_str()),
                                    key_val.get("e").and_then(|e| e.as_str()),
                                ) {
                                    if let Ok(dec_key) = DecodingKey::from_rsa_components(n, e) {
                                        map.insert(kid.to_string(), dec_key);
                                    }
                                }
                            }
                            *k.write().await = map;
                            tracing::info!("Apollo Router Native JWT Plugin: JWKS keys refreshed successfully in memory.");
                        }
                    }
                }
                tokio::time::sleep(tokio::time::Duration::from_secs(300)).await;
            }
        });

        Ok(JwtAuthPlugin { config, keys })
    }

    fn router_service(&self, service: router::BoxService) -> router::BoxService {
        let keys = self.keys.clone();
        let config = self.config.clone();

        ServiceBuilder::new()
            .map_future_with_subservice(move |service: router::BoxService, mut req: router::Request| {
                let keys = keys.clone();
                let config = config.clone();

                async move {
                    if let Some(auth_header) = req.router_request.headers().get("authorization") {
                        if let Ok(auth_str) = auth_header.to_str() {
                            if let Some(token) = auth_str.strip_prefix("Bearer ") {
                                if let Ok(header) = decode_header(token) {
                                    if let Some(kid) = header.kid {
                                        let guard = keys.read().await;
                                        if let Some(decoding_key) = guard.get(&kid) {
                                            let mut validation = Validation::new(Algorithm::RS256);
                                            validation.set_issuer(&[&config.issuer]);
                                            validation.validate_exp = true;

                                            if let Ok(token_data) = decode::<Claims>(token, decoding_key, &validation) {
                                                if let Some(sub) = token_data.claims.sub {
                                                    let _ = req.context.insert("verified_user_id", sub);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    service.oneshot(req).await
                }
            })
            .service(service)
            .boxed()
    }
}

apollo_router::register_plugin!("custom", "jwt_auth", JwtAuthPlugin);
