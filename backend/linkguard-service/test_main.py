import pytest
from fastapi.testclient import TestClient
from main import app, generate_signature, verify_signature, inspect_link_cache, LinkInspectionResult

client = TestClient(app)

def test_signature_verification():
    url = "https://safe-url.com/path"
    sig = generate_signature(url)
    assert verify_signature(url, sig) is True
    assert verify_signature(url, sig + "invalid") is False
    assert verify_signature("https://another-url.com", sig) is False

def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "UP"}

def test_link_shim_invalid_signature():
    url = "https://example.com"
    response = client.get(f"/l.php?url={url}&h=invalid_signature")
    assert response.status_code == 400
    assert "Tarcza Ochronna LinkGuard" in response.text
    assert "Sygnatura uwierzytelniająca link jest niepoprawna" in response.text

def test_link_shim_safe_redirect():
    url = "https://safe-example.com/page"
    signature = generate_signature(url)
    
    # Pre-populate inspect cache to avoid real network lookups
    inspect_link_cache[url] = LinkInspectionResult(
        url=url,
        is_safe=True,
        risk_score=10,
        shield_url="http://localhost:8080/l.php",
        flags=[]
    )
    
    # We expect 302 redirect for safe URLs
    response = client.get(f"/l.php?url={url}&h={signature}", follow_redirects=False)
    assert response.status_code == 302
    assert response.headers["location"] == url
    assert response.headers["referrer-policy"] == "no-referrer"

def test_link_shim_warning_page():
    url = "https://suspicious-example.com/page"
    signature = generate_signature(url)
    
    inspect_link_cache[url] = LinkInspectionResult(
        url=url,
        is_safe=True,
        risk_score=50, # 40-74 triggers warning
        shield_url="http://localhost:8080/l.php",
        flags=["SUSPICIOUS_KEYWORDS"]
    )
    
    # We expect a warning HTML page (status 200)
    response = client.get(f"/l.php?url={url}&h={signature}")
    assert response.status_code == 200
    assert "Opuszczasz bezpieczną strefę" in response.text
    assert url in response.text

def test_link_shim_blocked_page():
    url = "https://phishing-example.com/page"
    signature = generate_signature(url)
    
    inspect_link_cache[url] = LinkInspectionResult(
        url=url,
        is_safe=False,
        risk_score=85, # >= 75 triggers block
        shield_url="http://localhost:8080/l.php",
        flags=["PHISHING_PATTERN"]
    )
    
    # We expect blocked HTML page (status 430)
    response = client.get(f"/l.php?url={url}&h={signature}")
    assert response.status_code == 430
    assert "Strona została zablokowana" in response.text
    assert "Phishing / Scam" in response.text
