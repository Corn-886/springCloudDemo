package com.example.module;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
public class OrderBean {
    Integer prodId;
    String userId;
}
