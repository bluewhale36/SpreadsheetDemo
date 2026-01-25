package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.data.SheetsDataQueryObject;
import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.repository.SheetsRepository;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import com.example.spreadsheetdemo.herb.domain.queryspec.HerbLogQuerySpec;
import com.example.spreadsheetdemo.herb.mapper.HerbLogRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public Optional<List<HerbLog>> findAllByRowNumRange(int startRowNum, int endRowNum) {
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllColumnDataWithSpecificRowRange(startRowNum, endRowNum, null);
        try {
            List<HerbLog> result = dqo.select(querySpec, herbLogRowMapper);
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    public Optional<List<LocalDateTime>> findAllLoggedDateTimeByRowNumRange(int startRowNum, int endRowNum) {
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllDataWithSpecificDimensionRange("A", "A", startRowNum, endRowNum, null);
        try {
            List<HerbLog> result = dqo.select(querySpec, herbLogRowMapper);
            return result.isEmpty() ? Optional.empty() : Optional.of(result.stream().map(HerbLog::getLoggedDateTime).toList());
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    public int countAll() {
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllRowDataWithSpecificColumnRange("A", "A", null);
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
        HerbLogQuerySpec querySpec = HerbLogQuerySpec.ofAllColumnDataWithSpecificRowRange(entity.getRowNum(), entity.getRowNum(), null);
        try {
            return dqo.update(entity, querySpec, herbLogRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException(e.getMessage(), e);
        }
    }

    @Override
    public HerbLog deleteOne(HerbLog entity) {
        return null;
    }
}
