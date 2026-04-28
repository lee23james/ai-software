from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def test_job_match_returns_score_and_skill_gap():
    response = client.post(
        "/ai/job/match",
        json={
            "resumeText": "熟悉 Java、Spring Boot、Vue 和 MySQL。",
            "jobDescription": "要求掌握 Java、Spring Boot、MySQL，了解 Redis。"
        },
    )

    body = response.json()

    assert response.status_code == 200
    assert body["score"] >= 60
    assert "Redis" in body["missingSkills"]
    assert body["suggestion"]

