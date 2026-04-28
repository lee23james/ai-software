from fastapi import APIRouter

from app.schemas.match import JobMatchRequest
from app.services.match_service import calculate_match

router = APIRouter()


@router.post("/match")
def match_job(request: JobMatchRequest) -> dict:
    return calculate_match(request.resume_text, request.job_description)

