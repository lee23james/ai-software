package com.example.jobplatform.util;

/**
 * 岗位 salary_min / salary_max 与「元/月」口径对齐。
 * <p>
 * 部分数据源以「k」为单位存库（如 12、18 表示 12k–18k）；当区间上限 &lt; {@value #K_AS_YUAN_THRESHOLD} 时，
 * 按千元理解为元并乘以 1000。已以元存储的数据（如 12000、18000）保持不变。
 */
public final class JobSalaryMonthlyYuan {

    public static final int K_AS_YUAN_THRESHOLD = 1000;

    private JobSalaryMonthlyYuan() {
    }

    /**
     * @return 元/月的 [lo, hi]，若无效（hi&lt;=0）返回 null
     */
    public static int[] storedPairToMonthlyYuan(int rawMin, int rawMax) {
        int lo = rawMin;
        int hi = rawMax;
        if (lo > hi) {
            int t = lo;
            lo = hi;
            hi = t;
        }
        if (hi <= 0) {
            return null;
        }
        if (hi < K_AS_YUAN_THRESHOLD) {
            lo = lo * 1000;
            hi = hi * 1000;
        }
        return new int[] { lo, hi };
    }
}
