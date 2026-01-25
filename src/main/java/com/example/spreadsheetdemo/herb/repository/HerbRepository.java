package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.data.SheetsDataQueryObject;
import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.repository.SheetsRepository;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.domain.queryspec.HerbQuerySpec;
import com.example.spreadsheetdemo.herb.mapper.HerbRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class HerbRepository implements SheetsRepository<Herb> {

    private final SheetsDataQueryObject dqo;
    private final HerbRowMapper herbRowMapper;

    @Override
    public List<Herb> findAll() {
        try {
            return dqo.select(HerbQuerySpec.ofAllDataRange(null), herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    public Herb findByRowNum(int rowNum) {
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllColumnDataWithSpecificRowRange(rowNum, rowNum, null);
        try {
            return dqo.select(querySpec, herbRowMapper).get(0);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    public Optional<List<Herb>> findAllByName(String name) {
        try {
            Predicate<Herb> queryCondition = (herb) -> herb.getName().equals(name);
            List<Herb> result = dqo.select(HerbQuerySpec.ofAllDataRange(queryCondition), herbRowMapper);
            return result == null || result.isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    @Override
    public Herb save(Herb entity) {
        if (entity.getRowNum() == null) {
            return saveNew(entity);
        }
        return saveExisting(entity);
    }

    private Herb saveNew(Herb entity) {
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllDataRange(null);
        try {
            return dqo.insert(entity, querySpec, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    private Herb saveExisting(Herb entity) {
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllColumnDataWithSpecificRowRange(entity.getRowNum(), entity.getRowNum(), null);
        try {
            return dqo.update(entity, querySpec, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    @Override
    public Herb deleteOne(Herb entity) {
        return null;
    }

    public Herb deleteByRowNum(int rowNum) {
        Predicate<Herb> queryCondition = (herb) -> herb.getRowNum() == rowNum;
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllDataRange(queryCondition);
        try {
            return dqo.delete(querySpec, herbRowMapper).get(0);
        }  catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

}
