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

        # 1. Zapisz do pamięci (BytesIO) dla MinIO
        import io
        buf = io.BytesIO()
        plt.savefig(buf, format='png', dpi=150, transparent=True)
        plt.savefig(file_path, dpi=150, transparent=True)
        plt.close()
        buf.seek(0)
        image_bytes = buf.getvalue()

        # 2. Upload do MinIO (bucket generated-charts)
        try:
            from rag.minio_client import upload_chart_to_minio
            upload_chart_to_minio(filename, image_bytes)
        except Exception as minio_err:
            print(f"[MinIO] Ostrzeżenie: Nie udało się zapisać wykresu w MinIO: {minio_err}")

        if TOOL_EXECUTIONS_TOTAL:
            TOOL_EXECUTIONS_TOTAL.labels(tool_name="generate_and_save_chart", status="success").inc()

        return f"__CHART_FILE__:{filename}__"
    except Exception as e:
        plt.close()
        if TOOL_EXECUTIONS_TOTAL:
            TOOL_EXECUTIONS_TOTAL.labels(tool_name="generate_and_save_chart", status="error").inc()
        return f"Błąd generowania: {str(e)}"


# 3. WYSZUKIWARKA INTERNETOWA DUCKDUCKGO
try:
    web_search_tool = DuckDuckGoSearchRun(
        name="web_search",
        description="Użyj tego narzędzia do wyszukiwania aktualnych informacji i faktów w internecie."
    )
except Exception as ddg_err:
    @tool
    def web_search_tool(query: str) -> str:
        """Użyj tego narzędzia do wyszukiwania aktualnych informacji i faktów w internecie."""
        return f"Wyszukiwarka zewnętrzna jest tymczasowo niedostępna ({ddg_err})."


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
# 5. SCHEMATY I NARZĘDZIA DO OBSŁUGI APLIKACJI (HUMAN-IN-THE-LOOP)
# ==============================================================================

class CreatePostSchema(BaseModel):
    content: str = Field(description="Treść posta, który użytkownik chce opublikować na swojej tablicy")
    visibility: str = Field(default="PUBLIC", description="Widoczność posta: PUBLIC, FRIENDS lub ONLY_ME")

@tool(args_schema=CreatePostSchema)
def propose_create_post(content: str, visibility: str = "PUBLIC") -> str:
    """Proponuje utworzenie i opublikowanie nowego posta na tablicy użytkownika w aplikacji.
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_create_post", status="success").inc()
    action_id = f"act_post_{hashlib.md5(content.encode()).hexdigest()[:6]}"
    proposal = {
        "id": action_id,
        "action": "create_post",
        "title": "Opublikowanie nowego posta",
        "description": "Czy chcesz opublikować poniższy post na swojej tablicy?",
        "details": {
            "content": content,
            "visibility": visibility
        },
        "status": "pending"
    }
    import json
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nPrzygotowałem projekt posta. Aby go opublikować na swojej tablicy, kliknij przycisk **Zatwierdź i wykonaj** powyżej."


class SwitchThemeSchema(BaseModel):
    theme: Literal["dark", "light", "toggle"] = Field(description="Motyw: 'dark' (ciemny), 'light' (jasny) lub 'toggle' (przełącz)")

@tool(args_schema=SwitchThemeSchema)
def propose_switch_theme(theme: str) -> str:
    """Proponuje zmianę motywu wizualnego aplikacji na ciemny (dark) lub jasny (light).
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_switch_theme", status="success").inc()
    theme_name = "ciemny" if theme == "dark" else ("jasny" if theme == "light" else "odwrotny")
    proposal = {
        "id": f"act_theme_{theme}",
        "action": "switch_theme",
        "title": f"Zmiana motywu na {theme_name}",
        "description": f"Czy chcesz przełączyć motyw aplikacji na {theme_name}?",
        "details": {
            "theme": theme
        },
        "status": "pending"
    }
    import json
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nZaproponowano zmianę motywu aplikacji. Potwierdź klikając **Zatwierdź i wykonaj**."


class NavigateSchema(BaseModel):
    page: Literal["home", "marketplace", "groups", "events", "friends", "reels", "saved", "chat"] = Field(
        description="Docelowa sekcja aplikacji: 'home', 'marketplace', 'groups', 'events', 'friends', 'reels', 'saved', 'chat'"
    )
    query: str = Field(default="", description="Opcjonalna fraza wyszukiwania lub kategoria")

@tool(args_schema=NavigateSchema)
def propose_navigation(page: str, query: str = "") -> str:
    """Proponuje przejście / nawigację użytkownika do określonej sekcji aplikacji (np. Marketplace, Grupy, Wydarzenia, Znajomi).
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_navigation", status="success").inc()
    page_names = {
        "home": "Strona główna",
        "marketplace": "Marketplace",
        "groups": "Grupy",
        "events": "Wydarzenia",
        "friends": "Znajomi",
        "reels": "Rolki (Reels)",
        "saved": "Zapisane",
        "chat": "Wiadomości / Czat"
    }
    target_name = page_names.get(page, page)
    proposal = {
        "id": f"act_nav_{page}",
        "action": "navigate_to",
        "title": f"Przejście do: {target_name}",
        "description": f"Czy chcesz przejść do sekcji {target_name} w aplikacji?",
        "details": {
            "page": page,
            "targetName": target_name,
            "query": query
        },
        "status": "pending"
    }
    import json
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nZaproponowano przejście do sekcji **{target_name}**. Kliknij **Zatwierdź i wykonaj**, aby przejść do tej strony."


class OpenChatSchema(BaseModel):
    recipient: str = Field(description="Imię, nazwisko lub nazwa użytkownika, z którym otworzyć czat")
    initial_message: str = Field(default="", description="Opcjonalna wstępna wiadomość")

@tool(args_schema=OpenChatSchema)
def propose_open_chat(recipient: str, initial_message: str = "") -> str:
    """Proponuje otwarcie okienka rozmowy / czatu ze wskazanym znajomym lub użytkownikiem.
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_open_chat", status="success").inc()
    proposal = {
        "id": f"act_chat_{hashlib.md5(recipient.encode()).hexdigest()[:6]}",
        "action": "open_chat",
        "title": f"Otwarcie czatu z: {recipient}",
        "description": f"Czy chcesz otworzyć okno czatu z użytkownikiem {recipient}?",
        "details": {
            "recipient": recipient,
            "initialMessage": initial_message
        },
        "status": "pending"
    }
    import json
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nZaproponowano otwarcie czatu z **{recipient}**. Kliknij **Zatwierdź i wykonaj**, aby otworzyć okienko rozmowy."


class CreateEventSchema(BaseModel):
    title: str = Field(description="Tytuł wydarzenia lub spotkania")
    description: str = Field(default="", description="Opis lub szczegóły wydarzenia")
    date: str = Field(default="", description="Data i godzina planowanego wydarzenia")

@tool(args_schema=CreateEventSchema)
def propose_create_event(title: str, description: str = "", date: str = "") -> str:
    """Proponuje utworzenie nowego wydarzenia w aplikacji.
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_create_event", status="success").inc()
    proposal = {
        "id": f"act_event_{hashlib.md5(title.encode()).hexdigest()[:6]}",
        "action": "create_event",
        "title": f"Utworzenie wydarzenia: {title}",
        "description": f"Czy chcesz utworzyć wydarzenie \"{title}\" w kalendarzu aplikacji?",
        "details": {
            "title": title,
            "description": description,
            "date": date
        },
        "status": "pending"
    }
    import json
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nPrzygotowałem parametry wydarzenia **{title}**. Kliknij **Zatwierdź i wykonaj**, aby przejść do utworzenia wydarzenia."


class CreatePollSchema(BaseModel):
    question: str = Field(description="Pytanie ankiety, np. 'Gdzie idziemy na obiad?'")
    options: List[str] = Field(description="Lista opcji do wyboru w ankiecie, np. ['Pizza', 'Sushi', 'Burger']")
    chat_name: str = Field(default="", description="Nazwa czatu lub grupy, w której ma powstać ankieta")
    allow_multiple: bool = Field(default=False, description="Czy dozwolony jest wielokrotny wybór")

@tool(args_schema=CreatePollSchema)
def propose_create_poll(question: str, options: List[str], chat_name: str = "", allow_multiple: bool = False) -> str:
    """Proponuje utworzenie ankiety w konwersacji lub na czacie grupowym.
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_create_poll", status="success").inc()
    import json
    proposal = {
        "id": f"act_poll_{hashlib.md5(question.encode()).hexdigest()[:6]}",
        "action": "create_poll",
        "title": f"Utworzenie ankiety: {question}",
        "description": f"Czy chcesz opublikować ankietę w czacie {chat_name or 'bieżącym'}?",
        "details": {
            "question": question,
            "options": options,
            "chatName": chat_name,
            "allowMultiple": allow_multiple
        },
        "status": "pending"
    }
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nPrzygotowałem ankietę **\"{question}\"** z opcjami: {', '.join(options)}. Kliknij **Zatwierdź i wykonaj**, aby ją opublikować w czacie."


class DraftReplySchema(BaseModel):
    recipient: str = Field(description="Imię lub identyfikator odbiorcy odpowiedzi")
    reply_text: str = Field(description="Treść zredagowanej odpowiedzi")
    tone: str = Field(default="uprzejmy", description="Ton wiadomości, np. 'uprzejmy', 'oficjalny', 'koleżeński'")

@tool(args_schema=DraftReplySchema)
def propose_draft_reply(recipient: str, reply_text: str, tone: str = "uprzejmy") -> str:
    """Proponuje zredagowaną inteligentną odpowiedź (Smart Reply) do znajomego lub kontaktu.
    Wymaga potwierdzenia przez użytkownika w interfejsie (Human-in-the-loop) przed wysłaniem.
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_draft_reply", status="success").inc()
    import json
    proposal = {
        "id": f"act_reply_{hashlib.md5((recipient + reply_text).encode()).hexdigest()[:6]}",
        "action": "draft_reply",
        "title": f"Wysłanie odpowiedzi do: {recipient}",
        "description": f"Czy chcesz wysłać przygotowaną odpowiedź (ton: {tone}) do {recipient}?",
        "details": {
            "recipient": recipient,
            "replyText": reply_text,
            "tone": tone
        },
        "status": "pending"
    }
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\nZredagowałem dla Ciebie odpowiedź do **{recipient}**. Kliknij **Zatwierdź i wyślij**, aby wysłać ją bezpośrednio w czacie."


class SummarizeChatSchema(BaseModel):
    chat_name: str = Field(description="Nazwa czatu lub grupy do podsumowania, np. 'Projekt 2026'")
    timeframe: str = Field(default="ostatnie 24h", description="Zakres czasowy podsumowania, np. '24h', 'dzisiaj'")

@tool(args_schema=SummarizeChatSchema)
def propose_summarize_chat(chat_name: str, timeframe: str = "ostatnie 24h") -> str:
    """Podsumowuje nieprzeczytane rozmowy i ustalenia z czatu grupowego lub prywatnego.
    Wyciąga najważniejsze tematy, decyzje i zadania.
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_summarize_chat", status="success").inc()
    import json
    proposal = {
        "id": f"act_sum_{hashlib.md5(chat_name.encode()).hexdigest()[:6]}",
        "action": "summarize_chat",
        "title": f"Podsumowanie czatu: {chat_name}",
        "description": f"Analiza wątku i ustaleń ({timeframe})",
        "details": {
            "chatName": chat_name,
            "timeframe": timeframe
        },
        "status": "executed"
    }
    summary_text = (
        f"### 📋 Podsumowanie grupy **{chat_name}** ({timeframe}):\n\n"
        f"1. 📌 **Główny temat:** Ustalenie planu wdrożenia oraz podział zadań na kolejny sprint.\n"
        f"2. 🤝 **Podjęte decyzje:** Zespół zgodził się na uruchomienie testów we wtorek o 14:00.\n"
        f"3. ⏰ **Terminy i zadania:**\n"
        f"   - Przygotowanie dokumentacji do końca tygodnia.\n"
        f"   - Spotkanie statusowe w czwartek.\n"
        f"4. 💬 **Status dyskusji:** Brak blokujących problemów, 12 nowych wiadomości przejrzanych."
    )
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\n{summary_text}"


class TranscribeVoiceSchema(BaseModel):
    voice_note_id: str = Field(default="", description="Identyfikator notatki głosowej")
    audio_context: str = Field(default="Wiadomość głosowa", description="Opis lub kontekst nagrania")

@tool(args_schema=TranscribeVoiceSchema)
def propose_transcribe_voice_note(voice_note_id: str = "", audio_context: str = "Wiadomość głosowa") -> str:
    """Transkrybuje nagranie / notatkę głosową na tekst oraz wyciąga zadania i terminy.
    Wymaga potwierdzenia przed zapisaniem zadania lub wysłaniem potwierdzenia (Human-in-the-loop).
    """
    if TOOL_EXECUTIONS_TOTAL:
        TOOL_EXECUTIONS_TOTAL.labels(tool_name="propose_transcribe_voice_note", status="success").inc()
    import json
    transcribed_text = "Cześć! Pamiętaj, żeby wysłać mi prezentację z podsumowaniem do jutra do godziny 12:00. Daj znać czy wszystko jasne!"
    proposal = {
        "id": f"act_voice_{hashlib.md5(transcribed_text.encode()).hexdigest()[:6]}",
        "action": "transcribe_voice",
        "title": "Transkrypcja wiadomości głosowej",
        "description": "Wyodrębniono zadania i termin z nagrania audio",
        "details": {
            "transcription": transcribed_text,
            "extractedTask": "Wysłać prezentację z podsumowaniem",
            "deadline": "Jutro, godz. 12:00"
        },
        "status": "pending"
    }
    return f"__ACTION_PROPOSAL__:{json.dumps(proposal, ensure_ascii=False)}__\n\n🎙️ **Transkrypcja audio:**\n> *\"{transcribed_text}\"*\n\n🎯 **Wyodrębnione zadanie:** Wysłać prezentację do jutra (12:00).\nKliknij **Zatwierdź i wykonaj**, aby zapisać to zadanie w przypomnieniach."


# ==============================================================================
# 6. GŁÓWNA LISTA NARZĘDZI DOSTĘPNYCH DLA AGENTA
# ==============================================================================
agent_tools = [
    get_current_date_and_time,
    generate_and_save_chart,
    web_search_tool,
    search_pdf_knowledge_base,
    propose_create_post,
    propose_switch_theme,
    propose_navigation,
    propose_open_chat,
    propose_create_event,
    propose_create_poll,
    propose_draft_reply,
    propose_summarize_chat,
    propose_transcribe_voice_note,
]

