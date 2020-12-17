package org.example.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ProdHisExcelDto {
    private Date date;
    private Double nav;
    private Double accumNav;
    private Double dailyNavRate;
}
