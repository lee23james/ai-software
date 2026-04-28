from fastapi import APIRouter

from app.schemas.resume import ResumeParseRequest, ResumeSuggestionRequest
from app.services.resume_service import parse_resume, suggest_resume_updates

router = APIRouter()


@router.post("/parse")
def parse(request: ResumeParseRequest) -> dict:
    return parse_resume(request.resume_text)


@router.post("/suggestion")
def suggestion(request: ResumeSuggestionRequest) -> dict:
    return suggest_resume_updates(request.resume_text, request.target_job)

