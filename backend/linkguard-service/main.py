import re
import hmac
import hashlib
import uuid
import whois
import requests
import os
import httpx
import json
import redis
import asyncio
import socket
from datetime import datetime
from urllib.parse import urljoin, urlparse, unquote, quote
from typing import Optional, List
from cachetools import TTLCache

# L3 Cache: DNS Caching to prevent duplicate DNS lookups (5 minutes TTL)
dns_cache = TTLCache(maxsize=1000, ttl=300)
original_getaddrinfo = socket.getaddrinfo

def cached_getaddrinfo(*args, **kwargs):
    # Create a hashable key for arguments
    cache_key = (args, tuple(sorted(kwargs.items())))
    if cache_key in dns_cache:
        return dns_cache[cache_key]
    result = original_getaddrinfo(*args, **kwargs)
    dns_cache[cache_key] = result
    return result

# Apply the DNS caching monkey-patch
socket.getaddrinfo = cached_getaddrinfo

import fastapi
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import RedirectResponse, HTMLResponse
import strawberry
from strawberry.fastapi import GraphQLRouter
from bs4 import BeautifulSoup
from cachetools import TTLCache

# Import zadania z workera Celery
from worker import moderate_post_task

# Lista zaufanych domen do natychmiastowego przepuszczania (Bypass WHOIS/Heurystyki)
SAFE_DOMAINS = {
    "google.com", "www.google.com", "facebook.com", "www.facebook.com",
    "github.com", "www.github.com", "youtube.com", "www.youtube.com",
    "wikipedia.org", "pl.wikipedia.org", "microsoft.com", "www.microsoft.com",
    "apple.com", "www.apple.com", "twitter.com", "www.twitter.com",
    "linkedin.com", "www.linkedin.com", "instagram.com", "www.instagram.com"
}

# Połączenie z Redisem (L2 Cache)
REDIS_URL = os.getenv("CELERY_BROKER_URL", "redis://redis:6379/0")
try:
    redis_client = redis.from_url(REDIS_URL, socket_timeout=2)
except Exception:
    redis_client = None

def get_redis_cache(url: str) -> Optional[dict]:
    if not redis_client:
        return None
    try:
        url_hash = hashlib.sha256(url.encode('utf-8')).hexdigest()
        data = redis_client.get(f"linkguard:inspect:{url_hash}")
        if data:
            return json.loads(data)
    except Exception as e:
        print(f"[LinkGuard Cache] Redis read error: {e}")
    return None

def set_redis_cache(url: str, result_dict: dict):
    if not redis_client:
        return
    try:
        url_hash = hashlib.sha256(url.encode('utf-8')).hexdigest()
        # Zapisujemy do pamięci podręcznej na 24 godziny (86400 sekund)
        redis_client.setex(f"linkguard:inspect:{url_hash}", 86400, json.dumps(result_dict))
    except Exception as e:
        print(f"[LinkGuard Cache] Redis write error: {e}")

# Globalny, asynchroniczny klient HTTP z pulą połączeń (Connection Pool)
http_client = httpx.AsyncClient(
    timeout=httpx.Timeout(3.0, connect=2.0),
    limits=httpx.Limits(max_keepalive_connections=50, max_connections=100)
)

# ==========================================
# KONFIGURACJA BAZOWA
# ==========================================

# Pamięć podręczna na informacje o stronach (10 minut życia)
site_info_cache = TTLCache(maxsize=1000, ttl=600)

# Pamięć podręczna na inspekcje linków (10 minut życia)
inspect_link_cache = TTLCache(maxsize=1000, ttl=600)

# Pamięć podręczna na metadane Open Graph (10 minut życia)
scrape_og_cache = TTLCache(maxsize=1000, ttl=600)

# Klucz kryptograficzny do podpisywania linków przekierowań (Link Shim)
# UWAGA: Ten klucz MUSI być identyczny z tym zapisanym w FeedService (Java)
SECRET_KEY = os.getenv("LINK_SHIM_SECRET", "super_secret_link_guard_key_2026").encode('utf-8')

def generate_signature(url: str) -> str:
    """Tworzy unikalny podpis HMAC-SHA256 dla adresu URL."""
    return hmac.new(SECRET_KEY, url.encode('utf-8'), hashlib.sha256).hexdigest()

def verify_signature(url: str, signature: str) -> bool:
    """Weryfikuje czy podpis linku nie został sfałszowany w przeglądarce."""
    expected = generate_signature(url)
    return hmac.compare_digest(expected, signature)

# ==========================================
# TYPY DANYCH GRAPHQL
# ==========================================

@strawberry.type
class OgData:
    title: Optional[str] = None
    description: Optional[str] = None
    image: Optional[str] = None
    site_name: Optional[str] = None

@strawberry.type
class LinkInspectionResult:
    url: str
    is_safe: bool
    risk_score: int
    shield_url: str
    flags: List[str]

@strawberry.type
class SiteInformation:
    title: str
    description: str
    registration_date: str
    domain: str
    source: str
    image_url: str
    wiki_url: Optional[str] = None


class ModerationRequest(BaseModel):
    post_id: str
    content: str

def lookup_associated_page(url: str) -> Optional[dict]:
    # 1. First check if it is direct /pages/ or /profile/ link with UUID
    uuid_match = re.search(r'/(?:pages|profile)/([0-9a-fA-F\-]{36})', url)
    if uuid_match:
        page_id = uuid_match.group(1)
        try:
            res = requests.get(f"http://userservice:9090/api/pages/{page_id}", timeout=3)
            if res.status_code == 200:
                return res.json()
        except Exception:
            pass

    # 2. Check if the domain of the URL is registered in any platform page
    parsed = urlparse(url)
    if not parsed.netloc:
        return None
    
    # Get hostname (e.g. "www.meczyki.pl")
    domain = parsed.netloc.lower()
    
    # Try query with full domain (e.g. "www.meczyki.pl")
    try:
        res = requests.get(f"http://userservice:9090/api/pages/by-website?domain={domain}", timeout=3)
        if res.status_code == 200:
            return res.json()
    except Exception:
        pass
        
    # If the domain starts with "www.", also try without "www."
    if domain.startswith("www."):
        clean_domain = domain[4:]
        try:
            res = requests.get(f"http://userservice:9090/api/pages/by-website?domain={clean_domain}", timeout=3)
            if res.status_code == 200:
                return res.json()
        except Exception:
            pass
            
    # Conversely, if domain does NOT start with "www.", try with "www."
    else:
        www_domain = "www." + domain
        try:
            res = requests.get(f"http://userservice:9090/api/pages/by-website?domain={www_domain}", timeout=3)
            if res.status_code == 200:
                return res.json()
        except Exception:
            pass

    return None

# ==========================================
# ZAPYTANIA GRAPHQL (QUERIES)
# ==========================================

@strawberry.type
class Query:

    @strawberry.field
    def scrape_og(self, url: str) -> OgData:
        """Pobiera metadane Open Graph (w tym kluczowy og:image dla frontendu)."""
        if not url.startswith(('http://', 'https://')):
            return OgData()
        if url in scrape_og_cache:
            return scrape_og_cache[url]

        page_data = lookup_associated_page(url)
        if page_data:
            image = page_data.get("cover") or page_data.get("avatar") or "https://picsum.photos/1200/400"
            og_res = OgData(
                title=page_data.get("name") or "Strona platformy",
                description=page_data.get("bio") or page_data.get("category") or "Strona na platformie Facebook",
                image=image,
                site_name="Platform Pages"
            )
            scrape_og_cache[url] = og_res
            return og_res

        try:
            headers = {'User-Agent': 'facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)'}
            res = requests.get(url, headers=headers, timeout=5)
            if res.status_code != 200:
                return OgData()

            soup = BeautifulSoup(res.text, 'html.parser')
            og_data = {}
            for tag in ['og:title', 'og:description', 'og:image', 'og:site_name']:
                meta = soup.find('meta', property=tag)
                if meta and meta.get('content'):
                    og_data[tag.replace('og:', '')] = meta['content']

            # Fallback dla tytułu
            if 'title' not in og_data and soup.title:
                og_data['title'] = soup.title.string.strip()

            # Fallback dla obrazka (favicon)
            if not og_data.get('image'):
                icon = soup.find("link", rel=lambda x: x and 'icon' in x.lower())
                og_data['image'] = urljoin(url, icon['href']) if icon else f"{urlparse(url).scheme}://{urlparse(url).netloc}/favicon.ico"

            res_data = OgData(**og_data)
            scrape_og_cache[url] = res_data
            return res_data
        except Exception:
            res_data = OgData()
            scrape_og_cache[url] = res_data
            return res_data

    @strawberry.field
    async def inspect_link(self, url: str) -> LinkInspectionResult:
        """Analizuje link pod kątem ryzyka oszustwa przed otwarciem."""
        # L1 Cache (In-Memory) check
        if url in inspect_link_cache:
            return inspect_link_cache[url]

        # Whitelist (Safe Domains Bypass) check
        parsed_url = urlparse(url)
        initial_domain = parsed_url.netloc.lower()
        clean_domain = re.sub(r'^(www\.)?', '', initial_domain)
        if clean_domain in SAFE_DOMAINS or initial_domain in SAFE_DOMAINS:
            res = LinkInspectionResult(
                url=url,
                is_safe=True,
                risk_score=0,
                shield_url=url,
                flags=[]
            )
            inspect_link_cache[url] = res
            return res

        # L2 Cache (Redis) check
        cached_redis = get_redis_cache(url)
        if cached_redis:
            res = LinkInspectionResult(
                url=cached_redis["url"],
                is_safe=cached_redis["is_safe"],
                risk_score=cached_redis["risk_score"],
                shield_url=cached_redis["shield_url"],
                flags=cached_redis["flags"]
            )
            inspect_link_cache[url] = res
            return res

        flags = []
        risk_score = 0

        try:
            # Perform async HEAD request utilizing HTTP connection pooling and following redirects
            resolved_head = await http_client.head(url, follow_redirects=True)
            evaluated_url = str(resolved_head.url)
        except Exception:
            res = LinkInspectionResult(url=url, is_safe=False, risk_score=100, shield_url="", flags=["URL_UNREACHABLE"])
            inspect_link_cache[url] = res
            set_redis_cache(url, {
                "url": url, "is_safe": False, "risk_score": 100, "shield_url": "", "flags": ["URL_UNREACHABLE"]
            })
            return res

        parsed_domain = urlparse(evaluated_url).netloc.lower()

        # Check whitelist again for evaluated redirect target URL
        clean_eval_domain = re.sub(r'^(www\.)?', '', parsed_domain)
        if clean_eval_domain in SAFE_DOMAINS or parsed_domain in SAFE_DOMAINS:
            res = LinkInspectionResult(
                url=evaluated_url,
                is_safe=True,
                risk_score=0,
                shield_url=evaluated_url,
                flags=[]
            )
            inspect_link_cache[url] = res
            set_redis_cache(url, {
                "url": evaluated_url, "is_safe": True, "risk_score": 0, "shield_url": evaluated_url, "flags": []
            })
            return res

        # 1. Analiza heurystyczna słów wysokiego ryzyka
        phishing_triggers = ["login", "poczta", "secure", "verification", "uaktualnij", "bank", "pay-online"]
        if any(trigger in parsed_domain for trigger in phishing_triggers):
            risk_score += 45
            flags.append("SUSPICIOUS_DOMAIN_KEYWORDS")

        # 2. Sprawdzenie wieku domeny przez WHOIS (w osobnym wątku)
        try:
            domain_query = re.sub(r'^(www\.)?', '', parsed_domain)
            whois_data = await asyncio.to_thread(whois.whois, domain_query)
            if whois_data.creation_date:
                birth_date = whois_data.creation_date[0] if isinstance(whois_data.creation_date, list) else whois_data.creation_date
                age_days = (datetime.now() - birth_date).days
                if age_days < 30:
                    risk_score += 40
                    flags.append("FRESHLY_REGISTERED_DOMAIN")
        except Exception:
            pass

        base_domain = os.getenv("PLATFORM_BASE_URL", "http://localhost:8080")
        signature = generate_signature(evaluated_url)
        shield_url = f"{base_domain}/l.php?url={quote(evaluated_url)}&h={signature}"

        res = LinkInspectionResult(
            url=evaluated_url,
            is_safe=risk_score < 70,
            risk_score=min(risk_score, 100),
            shield_url=shield_url,
            flags=flags
        )
        inspect_link_cache[url] = res
        set_redis_cache(url, {
            "url": evaluated_url,
            "is_safe": risk_score < 70,
            "risk_score": min(risk_score, 100),
            "shield_url": shield_url,
            "flags": flags
        })
        return res

    @strawberry.field
    def get_site_info(self, url: str) -> SiteInformation:
        """Pobiera metadane o pochodzeniu strony (Wikipedia + WHOIS)."""
        if url in site_info_cache:
            return site_info_cache[url]

        page_data = lookup_associated_page(url)
        if page_data:
            extracted_domain = re.sub(r'^https?://(www\.)?', '', url).split('/')[0]
            registration_date = "Nieznana"
            try:
                w = whois.whois(extracted_domain)
                c_date = w.creation_date[0] if isinstance(w.creation_date, list) else w.creation_date
                if c_date:
                    diff_years = datetime.now().year - c_date.year
                    registration_date = f"Ponad {diff_years} lat temu" if diff_years > 0 else "W bieżącym roku"
            except Exception:
                pass

            structured_output = SiteInformation(
                title=page_data.get("name") or "Strona platformy",
                description=page_data.get("bio") or "Ta strona nie ma jeszcze biogramu.",
                registration_date=registration_date,
                domain=extracted_domain,
                source="Platforma",
                image_url=page_data.get("avatar") or "https://i.pravatar.cc/150",
                wiki_url=url
            )
            site_info_cache[url] = structured_output
            return structured_output

        extracted_domain = re.sub(r'^https?://(www\.)?', '', url).split('/')[0]
        search_keyword = extracted_domain.split('.')[0]

        response_template = {
            "title": extracted_domain.capitalize(),
            "description": "Brak szczegółowego opisu w Wikipedii.",
            "registration_date": "Nieznana",
            "domain": extracted_domain,
            "source": "Heurystyka Lokalna",
            "image_url": f"https://www.google.com/s2/favicons?sz=128&domain={extracted_domain}"
        }

        try:
            w = whois.whois(extracted_domain)
            c_date = w.creation_date[0] if isinstance(w.creation_date, list) else w.creation_date
            if c_date:
                diff_years = datetime.now().year - c_date.year
                response_template["registration_date"] = f"Ponad {diff_years} lat temu" if diff_years > 0 else "W bieżącym roku"
        except Exception:
            pass

        try:
            wiki_api = "https://pl.wikipedia.org/w/api.php"
            wiki_res = requests.get(wiki_api, params={"action": "query", "list": "search", "srsearch": search_keyword, "format": "json"}, timeout=3)
            if wiki_res.status_code == 200:
                search_nodes = wiki_res.json().get("query", {}).get("search", [])
                if search_nodes and search_keyword.lower() in search_nodes[0]["title"].lower():
                    target_page = search_nodes[0]["title"].replace(' ', '_')
                    summary_res = requests.get(f"https://pl.wikipedia.org/api/rest_v1/page/summary/{target_page}", timeout=3)
                    if summary_res.status_code == 200:
                        data = summary_res.json()
                        response_template["description"] = data.get("extract", response_template["description"])
                        response_template["title"] = data.get("title", response_template["title"])
                        response_template["source"] = "Wikipedia"
                        response_template["wiki_url"] = data.get("content_urls", {}).get("desktop", {}).get("page")
        except Exception:
            pass

        structured_output = SiteInformation(**response_template)
        site_info_cache[url] = structured_output
        return structured_output

# ==========================================
# MUTACJE GRAPHQL (MUTATIONS)
# ==========================================

@strawberry.type
class Mutation:
    @strawberry.mutation
    def enqueue_post_moderation(self, post_id: str, content: str) -> str:
        """Przekazuje post do analizy anty-scamowej przez model BERT."""
        moderate_post_task.delay(post_id, content)
        return "ENQUEUED"

# ==========================================
# KONFIGURACJA FASTAPI I BRAMKI
# ==========================================

schema = strawberry.Schema(query=Query, mutation=Mutation)
graphql_router = GraphQLRouter(schema)

app = fastapi.FastAPI(title="LinkGuard Core Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(graphql_router, prefix="/graphql")


@app.get("/health")
async def health_check():
    return {"status": "UP"}


@app.post("/moderation/posts", status_code=202)
async def enqueue_post_moderation(request: ModerationRequest):
    """Internal endpoint used by FeedService after a post is created."""
    moderate_post_task.delay(request.post_id, request.content)
    return {"status": "ENQUEUED", "post_id": request.post_id}

# NATIVE GATEWAY: Bramka Przekierowań (Facebook Link Shim Pattern)
@app.get("/l.php")
async def link_shim_gateway(url: str, h: str):
    target_url = unquote(url)

    # 1. Sprawdzenie podpisu HMAC (Ochrona przed manipulacją)
    if not verify_signature(target_url, h):
        return HTMLResponse(
            status_code=400,
            content="""
            <style>body { font-family: sans-serif; text-align: center; padding-top: 10%; background: #fafafa; color: #333; }</style>
            <div style="max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                <h2 style="color: #dc3545;">Tarcza Ochronna LinkGuard</h2>
                <p><strong>Błąd bezpieczeństwa:</strong> Sygnatura uwierzytelniająca link jest niepoprawna lub wygasła.</p>
                <p style="color: #666; font-size: 13px;">Modyfikacja adresu URL wewnątrz naszej platformy została zablokowana automatycznie.</p>
                <a href="/" style="display:inline-block; margin-top:15px; color:#007bff; text-decoration:none;">Powrót do Strony Głównej</a>
            </div>
            """
        )

    # 2. Dynamiczna ocena ryzyka domeny w locie
    inspection = await Query().inspect_link(target_url)

    # STAN A: Zablokowanie niebezpiecznej strony
    if inspection.risk_score >= 75:
        return HTMLResponse(
            status_code=430,
            content=f"""
            <style>body {{ font-family: sans-serif; text-align: center; padding-top: 10%; background: #fff5f5; }}</style>
            <div style="max-width: 550px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; border: 2px solid #dc3545;">
                <h2 style="color: #dc3545;">Strona została zablokowana</h2>
                <p>Nasz system zidentyfikował ten adres jako poważne zagrożenie typu <strong>Phishing / Scam</strong>.</p>
                <div style="background: #f8d7da; color: #721c24; padding: 15px; border-radius: 4px; word-break: break-all; font-family: monospace; margin: 20px 0;">
                    {target_url}
                </div>
            </div>
            """
        )

    # STAN B: Ostrzeżenie dla podejrzanych stron
    elif inspection.risk_score >= 40:
        return HTMLResponse(
            status_code=200,
            content=f"""
            <style>
                body {{ font-family: sans-serif; background: #fffdf6; display: flex; justify-content: center; padding-top: 8%; }}
                .card {{ max-width: 500px; background: white; padding: 35px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #ffeeba; text-align: center; }}
                .btn {{ display: inline-block; background: #ffc107; color: #212529; padding: 12px 25px; text-decoration: none; border-radius: 4px; font-weight: bold; margin-top: 20px; }}
            </style>
            <div class="card">
                <h3 style="color: #856404; margin-top: 0;">Opuszczasz bezpieczną strefę</h3>
                <p style="color: #444; font-size: 14px;">Próbujesz przejść do witryny zewnętrznej o wyższym stopniu ryzyka.</p>
                <div style="background: #fff3cd; padding: 12px; border-radius: 4px; word-break: break-all; font-family: monospace; font-size: 13px; margin: 15px 0; color: #856404;">
                    {target_url}
                </div>
                <a class="btn" href="{target_url}" rel="nofollow">Rozumiem, przejdź dalej</a>
                <a href="javascript:history.back()" style="display:inline-block; color:#6c757d; margin-left:15px; text-decoration:none;">Wróć</a>
            </div>
            """
        )

    # STAN C: Bezpieczne przekierowanie
    response = RedirectResponse(url=target_url, status_code=302)
    response.headers["Referrer-Policy"] = "no-referrer"
    return response

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8086)
