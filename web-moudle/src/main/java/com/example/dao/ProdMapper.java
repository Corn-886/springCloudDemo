package com.example.dao;

import com.example.module.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ProdMapper {
   Product queryAllProdById(@Param("id") Integer id);

   int selectTest();
}
