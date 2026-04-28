from app.services.skill_extractor import extract_skills


def calculate_match(resume_text: str, job_description: str) -> dict:
    resume_skills = set(extract_skills(resume_text))
    job_skills = set(extract_skills(job_description))

    matched_skills = sorted(resume_skills & job_skills)
    missing_skills = sorted(job_skills - resume_skills)

    if job_skills:
        score = round(60 + 40 * len(matched_skills) / len(job_skills))
    else:
        score = 60

    if missing_skills:
        suggestion = f"当前匹配度较好，建议补充 {', '.join(missing_skills)} 相关经历。"
    else:
        suggestion = "当前技能关键词与岗位要求匹配度较高，建议继续强化项目成果表达。"

    return {
        "score": min(score, 100),
        "matchedSkills": matched_skills,
        "missingSkills": missing_skills,
        "suggestion": suggestion,
    }

