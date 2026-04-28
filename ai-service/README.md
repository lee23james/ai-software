# AI Service

Python FastAPI service for resume parsing, job matching, and resume suggestions.

## Start

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## Test

```bash
source .venv/bin/activate
pytest
```

