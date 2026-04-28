from pydantic import BaseModel, ConfigDict, Field


class ResumeParseRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    resume_text: str = Field(alias="resumeText", min_length=1)


class ResumeSuggestionRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    resume_text: str = Field(alias="resumeText", min_length=1)
    target_job: str = Field(alias="targetJob", min_length=1)

