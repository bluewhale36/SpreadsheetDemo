package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
import com.example.spreadsheetdemo.common.data.SheetsDataQueryObject;
import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.repository.SheetsRepository;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import com.example.spreadsheetdemo.herb.domain.queryspec.HerbLogQuerySpec;
import com.example.spreadsheetdemo.herb.enums.HerbLogSheetColumnInfo;
import com.example.spreadsheetdemo.herb.mapper.HerbLogRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class HerbLogRepository implements SheetsRepository<HerbLog> {

    private final SheetsDataQueryObject dqo;
    private final HerbLogRowMapper herbLogRowMapper;

    @Override
    public List<HerbLog> findAll() {
        try {
            return dqo.select(HerbLogQuerySpec.ofAllDataRange(null), herbLogRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    public Optional<List<HerbLog>> findAllByLoggedDateTimeBetween(LocalDate fromExclude, LocalDate toInclude) {
        Predicate<HerbLog> queryCondition =
                (log) -> log.getLoggedDateTime().toLocalDate().isAfter(fromExclude) &&
                        (log.getLoggedDateTime().toLocalDate().isBefore(toInclude) || log.getLoggedDateTime().toLocalDate().isEqual(toInclude));
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllDataRange(queryCondition);
        try {
            List<HerbLog> result = dqo.select(querySpec, herbLogRowMapper);
            return result.isEmpty() ?
                    Optional.empty() :
                    Optional.of(result.stream().sorted(Comparator.comparing(HerbLog::getLoggedDateTime).reversed()).toList());
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    public Optional<List<HerbLog>> findAllByName(String name) {
        Predicate<HerbLog> queryCondition = (log) -> log.getName().equals(name);
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllDataRange(queryCondition);
        try {
            List<HerbLog> result = dqo.select(querySpec, herbLogRowMapper);
            return result.isEmpty() ?
                    Optional.empty() :
                    Optional.of(result.stream().sorted(Comparator.comparing(HerbLog::getLoggedDateTime).reversed()).toList());
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    public int countAll() {
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllRowDataRange(null, HerbLogSheetColumnInfo.LOGGED_DATE_TIME);
        try {
            List<HerbLog> result = dqo.select(querySpec, herbLogRowMapper);
            return result.size();
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    @Override
    public HerbLog save(HerbLog entity) {
        if (entity.getRowNum() == null) {
            return saveNew(entity);
        }
        return saveExisting(entity);
    }

    private HerbLog saveNew(HerbLog entity) {
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllDataRange(null);
        try {
            return dqo.insert(entity, querySpec, herbLogRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    private HerbLog saveExisting(HerbLog entity) {
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllColumnDataRange(entity.getRowNum(), entity.getRowNum(), null);
        try {
            return dqo.update(entity, querySpec, herbLogRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    @Override
    public List<HerbLog> saveAll(List<HerbLog> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }

        List<HerbLog>
                insertingEntityList = entityList.stream().filter(herb -> herb.getRowNum() == null).toList(),
                updatingEntityList = entityList.stream().filter(herb -> herb.getRowNum() != null).toList(),
                savedEntityList = new ArrayList<>();

        savedEntityList.addAll(saveAllNew(insertingEntityList));
        savedEntityList.addAll(saveAllExisting(updatingEntityList));

        return savedEntityList;
    }

    private List<HerbLog> saveAllNew(List<HerbLog> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        try {
            return dqo.insertAll(entityList, SheetsInfo.HERB_LOG, herbLogRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    private List<HerbLog> saveAllExisting(List<HerbLog> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        try {
            return dqo.updateAll(entityList, SheetsInfo.HERB_LOG, herbLogRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("API 통신 오류.", e);
        }
    }

    @Override
    public HerbLog deleteOne(HerbLog entity) {
        return null;
    }
}
