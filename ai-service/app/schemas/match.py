from pydantic import BaseModel, ConfigDict, Field


class JobMatchRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    resume_text: str = Field(alias="resumeText", min_length=1)
    job_description: str = Field(alias="jobDescription", min_length=1)

