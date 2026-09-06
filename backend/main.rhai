fn supergraph_service(service) {
  service.map_request(Fn("store_actor_header"));
}

fn subgraph_service(service, subgraph) {
  service.map_request(Fn("apply_kong_identity"));
}

fn store_actor_header(request) {
  let actor_id = "";
  if request.headers.contains("x-actor-id") {
    actor_id = request.headers["x-actor-id"];
  }
  request.context["identity::x_actor_id"] = actor_id;
}

fn claim_string(claims, name) {
  if !claims.contains(name) {
    return "";
  }
  let value = claims[name];
  if value == () {
    return "";
  }
  return value.to_string();
}

fn apply_kong_identity(request) {
  // Strip any spoofed headers from untrusted clients
  if request.subgraph.headers.contains("x-user-id") {
    request.subgraph.headers.remove("x-user-id");
  }
  if request.subgraph.headers.contains("x-actor-id") {
    request.subgraph.headers.remove("x-actor-id");
  }
  if request.subgraph.headers.contains("x-entity-type") {
    request.subgraph.headers.remove("x-entity-type");
  }
  if request.subgraph.headers.contains("x-original-user-id") {
    request.subgraph.headers.remove("x-original-user-id");
  }

  let claims = request.context[Router.APOLLO_AUTHENTICATION_JWT_CLAIMS];
  if claims == () {
    return;
  }

  let sub = claim_string(claims, "sub");
  let entity_type = claim_string(claims, "entity_type");
  let page_id = claim_string(claims, "page_id");
  let user_id = claim_string(claims, "user_id");

  let actor_id_header = request.context["identity::x_actor_id"];
  if actor_id_header == () {
    actor_id_header = "";
  }
  actor_id_header = actor_id_header.to_string();

  if entity_type == "PAGE" || page_id != "" {
    let effective_actor = page_id;
    if effective_actor == "" {
      effective_actor = sub;
    }
    request.subgraph.headers["x-user-id"] = effective_actor;
    request.subgraph.headers["x-actor-id"] = effective_actor;
    request.subgraph.headers["x-entity-type"] = "PAGE";
    if user_id != "" {
      request.subgraph.headers["x-original-user-id"] = user_id;
    }
  } else if actor_id_header != "" && actor_id_header != sub {
    request.subgraph.headers["x-user-id"] = actor_id_header;
    request.subgraph.headers["x-actor-id"] = actor_id_header;
    request.subgraph.headers["x-original-user-id"] = sub;
    request.subgraph.headers["x-entity-type"] = "PAGE";
  } else if sub != "" {
    request.subgraph.headers["x-user-id"] = sub;
    request.subgraph.headers["x-actor-id"] = sub;
    request.subgraph.headers["x-entity-type"] = "USER";
  }
}
