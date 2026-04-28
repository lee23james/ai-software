from fastapi import FastAPI

from app.routers import health, match, resume

app = FastAPI(
    title="Job Platform AI Service",
    description="AI service skeleton for resume parsing and job matching.",
    version="0.1.0",
)

app.include_router(health.router)
app.include_router(resume.router, prefix="/ai/resume", tags=["resume"])
app.include_router(match.router, prefix="/ai/job", tags=["match"])

