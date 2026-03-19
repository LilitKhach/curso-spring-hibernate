package com.finanalizador.dto;

import java.math.BigDecimal;

public class DashboardSummaryDTO {
    private int totalSocias;
    private BigDecimal totalBeneficio;
    private BigDecimal averageBeneficio;

    public DashboardSummaryDTO(int totalSocias, BigDecimal totalBeneficio, BigDecimal averageBeneficio) {
        this.totalSocias = totalSocias;
        this.totalBeneficio = totalBeneficio;
        this.averageBeneficio = averageBeneficio;
    }

    // Getters and setters
    public int getTotalSocias() {
        return totalSocias;
    }

    public void setTotalSocias(int totalSocias) {
        this.totalSocias = totalSocias;
    }

    public BigDecimal getTotalBeneficio() {
        return totalBeneficio;
    }

    public void setTotalBeneficio(BigDecimal totalBeneficio) {
        this.totalBeneficio = totalBeneficio;
    }

    public BigDecimal getAverageBeneficio() {
        return averageBeneficio;
    }

    public void setAverageBeneficio(BigDecimal averageBeneficio) {
        this.averageBeneficio = averageBeneficio;
    }
}
