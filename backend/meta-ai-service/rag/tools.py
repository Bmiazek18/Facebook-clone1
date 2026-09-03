"""
Meta AI - Narzędzia Agenta (Agent Tools)

W tym pliku definiujesz wszystkie narzędzia, z których może korzystać Twój asystent AI (Qwen 2.5).
Aby dodać nowe narzędzie:
1. Użyj dekoratora @tool (lub @tool(args_schema=TwojaKlasaPydantic) jeśli narzędzie przyjmuje złożone parametry).
2. Napisz dokładny docstring (opis funkcji) - na jego podstawie model LLM decyduje, kiedy wywołać to narzędzie.
3. Dodaj nazwę funkcji do listy `agent_tools` na dole pliku.
"""

import os
import hashlib
from datetime import datetime
from typing import List, Literal
from pydantic import BaseModel, Field
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

from langchain.tools import tool
from langchain_community.tools import DuckDuckGoSearchRun

try:
    from config.observability import TOOL_EXECUTIONS_TOTAL
except ImportError:
    TOOL_EXECUTIONS_TOTAL = None

CHARTS_DIR = "generated_charts"


# ==============================================================================
# 1. SCHEMATY PYDANTIC DLA ZŁOŻONYCH NARZĘDZI
# ==============================================================================

class ChartConfig(BaseModel):
    title: str = Field(
        description="Tytuł wykresu opisujący co przedstawiają dane, np. 'Cena chleba w weekend'"
    )
    type: Literal["bar", "line", "pie"] = Field(
        description="Typ wykresu: 'bar' (słupkowy), 'line' (liniowy) lub 'pie' (kołowy)"
    )
    labels: List[str] = Field(
        description="Etykiety dla osi X lub sekcji wykresu kołowego, np. ['Piątek', 'Sobota']"
    )
    values: List[float] = Field(
        description="Wartości liczbowe odpowiadające etykietom (musi być ich dokładnie tyle samo co etykiet)"
    )
    dark_mode: bool = Field(
        default=True,
        description="True jeśli wykres ma pasować do ciemnego motywu aplikacji, False dla jasnego motywu"
    )


# ==============================================================================
# 2. DEFINICJE NARZĘDZI AGENTA
# ==============================================================================

@tool
def get_current_date_and_time(query: str = "") -> str:
    """Zwraca aktualną datę, godzinę oraz dzień tygodnia.
    Używaj tego narzędzia zawsze, gdy użytkownik pyta o dzisiejszy dzień, aktualną godzinę lub rok.
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="get_current_date_and_time", status="success").inc()
    now = datetime.now()
    return f"Aktualna data i godzina to: {now.strftime('%Y-%m-%d %H:%M:%S')}, dzień tygodnia: {now.strftime('%A')}."


@tool(args_schema=ChartConfig)
def generate_and_save_chart(title: str, type: str, labels: list, values: list, dark_mode: bool = True) -> str:
    """Generuje wykres słupkowy, liniowy lub kołowy i zapisuje go jako obraz PNG.
    Używaj tego narzędzia zawsze, gdy użytkownik prosi o wizualizację danych, wykres lub diagram.
    """
    try:
        if not os.path.exists(CHARTS_DIR):
            os.makedirs(CHARTS_DIR)

        chart_type = type.lower()

        if not labels or not values or len(labels) != len(values):
            return "Błąd: Liczba etykiet musi odpowiadać liczbie wartości."

        plt.rcParams.update(plt.rcParamsDefault)
        plt.figure(figsize=(10, 6))
        plt.rcParams['font.family'] = 'sans-serif'

        if dark_mode:
            text_color = '#E2E8F0'
            grid_color = '#334155'
            accent_color = '#38BDF8'
            line_color = '#34D399'

            plt.rcParams['text.color'] = text_color
            plt.rcParams['axes.labelcolor'] = text_color
            plt.rcParams['xtick.color'] = text_color
            plt.rcParams['ytick.color'] = text_color
            plt.rcParams['axes.edgecolor'] = grid_color
        else:
            text_color = '#1E293B'
            grid_color = '#E2E8F0'
            accent_color = '#0EA5E9'
            line_color = '#10B981'

        plt.gca().set_axisbelow(True)
        plt.grid(True, linestyle='--', alpha=0.5, color=grid_color)

        if chart_type == "bar":
            plt.bar(labels, values, color=accent_color, edgecolor=text_color, alpha=0.85, width=0.6)
        elif chart_type == "line":
            plt.plot(labels, values, marker='o', linewidth=2.5, color=line_color, markersize=8)
        elif chart_type == "pie":
            cmap = plt.get_cmap('Pastel1' if not dark_mode else 'Set2')
            slices, texts, autotexts = plt.pie(
                values, labels=labels, autopct='%1.1f%%', startangle=140,
                wedgeprops={'edgecolor': grid_color, 'linewidth': 1, 'antialiased': True}
            )
            for t in texts:
                t.set_color(text_color)
            for at in autotexts:
                at.set_color('#000000' if not dark_mode else '#FFFFFF')
                at.set_weight('bold')

        plt.title(title, color=text_color, fontsize=14, pad=20, weight='bold')

        if chart_type != "pie":
            plt.gca().spines['top'].set_visible(False)
            plt.gca().spines['right'].set_visible(False)

        plt.tight_layout()

        filename = f"chart_{hashlib.md5(title.encode()).hexdigest()[:8]}.png"
        file_path = os.path.join(CHARTS_DIR, filename)

        plt.savefig(file_path, dpi=150, transparent=True)
        plt.close()

        if TOOL_EXECUTIONS_TOTAL:
            TOOL_EXECUTIONS_TOTAL.labels(tool_name="generate_and_save_chart", status="success").inc()

        return f"__CHART_FILE__:{file_path}__"
    except Exception as e:
        plt.close()
        if TOOL_EXECUTIONS_TOTAL:
            TOOL_EXECUTIONS_TOTAL.labels(tool_name="generate_and_save_chart", status="error").inc()
        return f"Błąd generowania: {str(e)}"


# 3. WYSZUKIWARKA INTERNETOWA DUCKDUCKGO
web_search_tool = DuckDuckGoSearchRun(
    name="web_search",
    description="Użyj tego narzędzia do wyszukiwania aktualnych informacji i faktów w internecie."
)


# 4. BAZA WEKTOROWA CHROMADB (RAG)
CHROMA_HOST = os.getenv("CHROMA_HOST", "")
CHROMA_PORT = int(os.getenv("CHROMA_PORT", "8000"))

def get_chroma_collection(collection_name="pdf_documents"):
    try:
        import chromadb
        if CHROMA_HOST:
            client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
        else:
            client = chromadb.PersistentClient(path="./chroma_db")
        return client.get_or_create_collection(name=collection_name, metadata={"hnsw:space": "cosine"})
    except Exception as e:
        print(f"[CHROMA ERROR]: {e}")
        return None


@tool
def search_pdf_knowledge_base(query: str) -> str:
    """Wyszukuje fragmenty wiedzy i dokumentów w bazie wektorowej ChromaDB.
    Używaj tego narzędzia zawsze, gdy użytkownik pyta o zawartość wgranych dokumentów, PDF, instrukcji lub projektu.
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="search_pdf_knowledge_base", status="success").inc()
    try:
        coll = get_chroma_collection()
        if not coll:
            return "Baza wektorowa jest obecnie niedostępna."
        results = coll.query(query_texts=[query], n_results=4)
        documents = results.get("documents", [[]])[0]
        if not documents:
            return "Nie znaleziono pasujących informacji w bazie dokumentów."
        return "\n\n---\n\n".join(documents)
    except Exception as e:
        if TOOL_EXECUTIONS_TOTAL:
            TOOL_EXECUTIONS_TOTAL.labels(tool_name="search_pdf_knowledge_base", status="error").inc()
        return f"Błąd przeszukiwania bazy wektorowej: {str(e)}"


# ==============================================================================
# 5. GŁÓWNA LISTA NARZĘDZI DOSTĘPNYCH DLA AGENTA
# ==============================================================================
# TUTAJ DOPISUJESZ KAŻDE NOWE NARZĘDZIE:
agent_tools = [
    get_current_date_and_time,
    generate_and_save_chart,
    web_search_tool,
    search_pdf_knowledge_base,
]

