KNOWN_SKILLS = [
    "Java",
    "Spring Boot",
    "MySQL",
    "Vue",
    "Element Plus",
    "ECharts",
    "Redis",
    "Docker",
    "Python",
    "SQL",
    "Excel",
]


def extract_skills(text: str) -> list[str]:
    normalized_text = text.lower()
    return [skill for skill in KNOWN_SKILLS if skill.lower() in normalized_text]

