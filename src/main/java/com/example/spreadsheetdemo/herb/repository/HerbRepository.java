package com.example.spreadsheetdemo.herb.repository;

import com.example.spreadsheetdemo.common.SheetsInfo;
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

@Component
@RequiredArgsConstructor
public class HerbRepository implements SheetsRepository<Herb> {

    private final SheetsDataQueryObject dqo;
    private final HerbRowMapper herbRowMapper;

    @Override
    public List<Herb> findAll() {
        try {
            return dqo.select(new HerbQuerySpec(), herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("약재 재고 정보를 불러오는 데 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    public Herb findByRowNum(int rowNum) {
        HerbQuerySpec querySpec = new HerbQuerySpec(
                SheetsInfo.HERB.getStartColumn(), SheetsInfo.HERB.getEndColumn(), rowNum, rowNum
        );
        try {
            return dqo.select(querySpec, herbRowMapper).get(0);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("약재 재고 정보를 불러오는 데 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    public Optional<List<Herb>> findAllByName(String name) {
        List<Herb> result = findAll().stream().filter(herb -> herb.getName().equals(name)).toList();
        return !result.isEmpty() ? Optional.of(result) : Optional.empty();
    }

    @Override
    public String save(Herb entity) {
        if (entity.getRowNum() == null) {
            return saveNew(entity);
        }
        return saveExisting(entity);
    }

    private String saveNew(Herb entity) {
        HerbQuerySpec querySpec = new HerbQuerySpec(
                SheetsInfo.HERB.getStartColumn(), SheetsInfo.HERB.getEndColumn(), null, null
        );
        try {
            return dqo.insert(entity, querySpec, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("약재 등록에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    private String saveExisting(Herb entity) {
        HerbQuerySpec querySpec = new HerbQuerySpec(
                SheetsInfo.HERB.getStartColumn(), SheetsInfo.HERB.getEndColumn(), entity.getRowNum(), entity.getRowNum()
        );
        try {
            return dqo.update(entity, querySpec, herbRowMapper);
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("약재 정보 수정에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    @Override
    public String deleteOne(Herb entity) {
        return null;
    }

    public String deleteByRowNum(int rowNum) {
        HerbQuerySpec querySpec = new HerbQuerySpec(
                SheetsInfo.HERB.getStartColumn(), SheetsInfo.HERB.getEndColumn(), rowNum, rowNum
        );
        try {
            return dqo.delete(querySpec);
        }  catch (GeneralSecurityException | IOException e) {
            throw new GoogleSpreadsheetsAPIException("약재 정보 삭제에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }
}
