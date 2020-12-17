package com.example.module;

import lombok.Data;

import java.io.Serializable;

@Data
public class Product implements Serializable {
     private int id;
     private String name;
     private Integer stock;
     private float price;
}
