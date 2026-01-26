package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllColumnDataRange(rowNum, rowNum, null);
        try {
            return dqo.select(querySpec, herbRowMapper).get(0);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    public Optional<List<Herb>> findAllByRowNums(Set<Integer> rowNumSet) {
        if (rowNumSet == null || rowNumSet.isEmpty()) {
            return Optional.empty();
        }
        Predicate<Herb> queryCondition = (herb) -> rowNumSet.contains(herb.getRowNum());
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllDataRange(queryCondition);
        try {
            List<Herb> result = dqo.select(querySpec, herbRowMapper);
            return result == null ? Optional.empty() : Optional.of(result);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    public Optional<List<Herb>> findAllByName(String name) {
        Predicate<Herb> queryCondition = (herb) -> herb.getName().equals(name);
        try {
            List<Herb> result = dqo.select(HerbQuerySpec.ofAllDataRange(queryCondition), herbRowMapper);
            return result == null || result.isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    public Optional<List<Herb>> findAllByNameContains(String keyword) {
        Predicate<Herb> queryCondition = (herb) -> herb.getName().contains(keyword);
        try {
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
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllColumnDataRange(entity.getRowNum(), entity.getRowNum(), null);
        try {
            return dqo.update(entity, querySpec, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    @Override
    public List<Herb> saveAll(List<Herb> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }

        List<Herb>
                insertingEntityList = entityList.stream().filter(herb -> herb.getRowNum() == null).toList(),
                updatingEntityList = entityList.stream().filter(herb -> herb.getRowNum() != null).toList(),
                savedEntityList = new ArrayList<>();

        savedEntityList.addAll(saveAllNew(insertingEntityList));
        savedEntityList.addAll(saveAllExisting(updatingEntityList));

        return savedEntityList;
    }

    private List<Herb> saveAllNew(List<Herb> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        try {
            return dqo.insertAll(entityList, SheetsInfo.HERB, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    private List<Herb> saveAllExisting(List<Herb> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        try {
            return dqo.updateAll(entityList, SheetsInfo.HERB, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    @Override
    public Herb deleteOne(Herb entity) {
        return null;
    }

    public void deleteByRowNum(int rowNum) {
        HerbQuerySpec querySpec = HerbQuerySpec.ofAllColumnDataRange(rowNum, rowNum, null);
        try {
            dqo.delete(querySpec, herbRowMapper);
        }  catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

}
