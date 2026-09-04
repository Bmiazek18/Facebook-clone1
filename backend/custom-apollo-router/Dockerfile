FROM rust:1.80-alpine AS builder

RUN apk add --no-cache musl-dev pkgconfig openssl-dev openssl-libs-static gcc g++ make

WORKDIR /app

COPY Cargo.toml ./
# Build dependencies layer
RUN mkdir src && echo "fn main() {}" > src/main.rs && cargo build --release || true

# Copy source code
COPY src/ ./src/
RUN touch src/main.rs && cargo build --release

# Runtime image
FROM alpine:3.20

RUN apk add --no-cache ca-certificates libgcc curl

WORKDIR /dist

COPY --from=builder /app/target/release/custom-apollo-router /dist/router

EXPOSE 4000 8088

ENTRYPOINT ["/dist/router"]
CMD ["-c", "/dist/config/router.yaml", "-s", "/dist/config/supergraph.graphql"]
