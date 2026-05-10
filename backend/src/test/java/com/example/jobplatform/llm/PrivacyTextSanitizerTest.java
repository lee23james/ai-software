package com.example.jobplatform.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrivacyTextSanitizerTest {

    @Test
    void redacts18DigitId() {
        String in = "身份证110101199003071234";
        assertThat(PrivacyTextSanitizer.sanitizePersonalContent(in)).doesNotContain("199003071234");
        assertThat(PrivacyTextSanitizer.sanitizePersonalContent(in)).contains("[已脱敏]");
    }

    @Test
    void redactsIdAfterLabel() {
        String in = "证件号：110101900030712";
        assertThat(PrivacyTextSanitizer.sanitizePersonalContent(in)).contains("[已脱敏]");
    }

    @Test
    void redactsMobileAndEmail() {
        String in = "手机13812345678 邮箱a@b.com";
        String out = PrivacyTextSanitizer.sanitizePersonalContent(in);
        assertThat(out).doesNotContain("13812345678").doesNotContain("a@b.com");
    }

    @Test
    void redactsLabeledName() {
        String in = "姓名：王小明\n项目经验";
        assertThat(PrivacyTextSanitizer.sanitizePersonalContent(in)).contains("姓名：[已脱敏]");
    }

    @Test
    void stripsCsvImportPhraseFromJobText() {
        String in = "由 CSV 样本导入的岗位信息。要求Java。";
        assertThat(PrivacyTextSanitizer.sanitizeJobPostingText(in)).doesNotContain("CSV");
        assertThat(PrivacyTextSanitizer.sanitizeJobPostingText(in)).contains("Java");
    }
}
