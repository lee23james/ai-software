package com.example.jobplatform.llm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在向外部大模型发送文本前做最小必要脱敏，并去除内部数据导入说明用语。
 */
public final class PrivacyTextSanitizer {

    private static final String REDACTED = "[已脱敏]";
    private static final Pattern ID_CARD_18 = Pattern.compile("(?<![0-9Xx])\\d{17}[\\dXx](?![0-9])");
    /** 旧版 15 位身份证（易与其它长数字冲突，仅在与「身份证」关键词邻近时脱敏） */
    private static final Pattern ID_NEAR_LABEL = Pattern.compile(
        "(?:身份证|身份证件号|证件号)([：:\\s]*)(\\d{15}|\\d{17}[\\dXx])"
    );
    private static final Pattern MAINLAND_MOBILE = Pattern.compile("(?<![0-9])1[3-9]\\d{9}(?![0-9])");
    private static final Pattern EMAIL = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    /** 常见「姓名：xxx」类行内值脱敏（含 2～10 个汉字或间隔点） */
    private static final Pattern LABELED_NAME = Pattern.compile(
        "(姓名|名字|联系人|法定姓名|曾用名)([：:\\s]+)([\\u4e00-\\u9fa5·．.\\s]{2,10})(?=[\\s,，;；\\|\\)\\]】]|$)"
    );
    private static final Pattern CSV_IMPORT_PHRASES = Pattern.compile(
        "(?i)(由\\s*CSV\\s*样本导入的岗位信息|CSV\\s*样本导入|从\\s*CSV\\s*导入|csv\\s*导入)[^。\\n\\r]*[。]?",
        Pattern.MULTILINE
    );

    private PrivacyTextSanitizer() {
    }

    /**
     * 简历正文、用户可见名称等：脱敏证件号、手机、邮箱、带标签的姓名片段。
     */
    public static String sanitizePersonalContent(String text) {
        if (text == null || text.isBlank()) {
            return text == null ? "" : text;
        }
        String t = text;
        t = ID_CARD_18.matcher(t).replaceAll(REDACTED);
        t = ID_NEAR_LABEL.matcher(t).replaceAll("身份证$1" + REDACTED);
        t = MAINLAND_MOBILE.matcher(t).replaceAll(REDACTED);
        t = EMAIL.matcher(t).replaceAll(REDACTED);
        t = scrubLabeledNames(t);
        return t;
    }

    private static String scrubLabeledNames(String input) {
        Matcher m = LABELED_NAME.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + m.group(2) + REDACTED));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 岗位描述等：去掉内部数据导入说明，再对个人隐私模式脱敏（防止描述中混入联系方式）。
     */
    public static String sanitizeJobPostingText(String text) {
        if (text == null || text.isBlank()) {
            return text == null ? "" : text;
        }
        String t = CSV_IMPORT_PHRASES.matcher(text).replaceAll("");
        t = sanitizePersonalContent(t);
        t = t.replaceAll("\\s{2,}", " ").trim();
        return t;
    }
}
