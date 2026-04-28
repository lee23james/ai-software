from app.services.skill_extractor import extract_skills


def parse_resume(resume_text: str) -> dict:
    skills = extract_skills(resume_text)
    return {
        "education": "本科",
        "major": "软件工程",
        "skills": skills,
        "projects": ["招聘数据分析平台"],
    }


def suggest_resume_updates(resume_text: str, target_job: str) -> dict:
    skills = extract_skills(resume_text)
    return {
        "targetJob": target_job,
        "keywords": skills,
        "suggestions": [
            "补充与目标岗位相关的技能关键词。",
            "将课程设计或项目经历改写为问题、行动、结果结构。",
            "突出数据库、接口开发、前后端联调等可验证成果。",
        ],
    }

