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

    String save(E entity);

    String deleteOne(E entity);

}
