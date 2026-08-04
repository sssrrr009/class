package com.classmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Excel 导入结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private Integer total;
    private Integer success;
    private List<RowError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private Integer row;
        private String message;
    }
}
