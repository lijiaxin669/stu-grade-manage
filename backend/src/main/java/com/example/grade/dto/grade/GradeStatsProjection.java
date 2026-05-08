package com.example.grade.dto.grade;

import java.math.BigDecimal;

public interface GradeStatsProjection {
    Double getAvgScore();
    Long getTotalCount();
    BigDecimal getMaxScore();
    BigDecimal getMinScore();
}
