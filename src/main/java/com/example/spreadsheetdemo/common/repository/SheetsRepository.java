package com.example.spreadsheetdemo.common.repository;

import com.example.spreadsheetdemo.common.domain.entity.SheetsEntity;
import com.example.spreadsheetdemo.common.domain.queryspec.SheetsQuerySpec;

import java.util.ArrayList;
import java.util.List;


public interface SheetsRepository<E extends SheetsEntity> {

    /*
        findAll(), save(), deleteOne() 등 Repository Interface 공통 method 정의
     */

    List<E> findAll();

    E save(E entity);

    List<E> saveAll(List<E> entityList);

    E deleteOne(E entity);

}
