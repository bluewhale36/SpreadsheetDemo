package com.example.spreadsheetdemo.herb.service;

import com.example.spreadsheetdemo.common.SheetsInfo;
import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.exception.OptimisticLockingException;
import com.example.spreadsheetdemo.common.exception.RollbackFailedException;
import com.example.spreadsheetdemo.herb.dto.*;
import com.example.spreadsheetdemo.herb.mapper.HerbMapper;
import com.example.spreadsheetdemo.herb.repository.HerbLogRepository;
import com.example.spreadsheetdemo.herb.repository.HerbRepository;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class HerbService {

    private final HerbRepository herbRepository;
    private final HerbLogRepository herbLogRepository;
    private final HerbMapper herbMapper;

    /**
     * 약재 정보가 담긴 스프레드시트의 모든 행을 조회.
     *
     * @return 스프레드시트의 모든 행 정보 {@link ValueRange}.
     */
    public List<HerbDTO> getAllHerbs() {
        ValueRange result;
        try {
            result = herbRepository.selectAll();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error fetching herb data: {}", e.getMessage());
            throw new GoogleSpreadsheetsAPIException("약재 재고 정보를 불러오는 데 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
        return herbMapper.toHerbDTOList(result);
    }

    public HerbDTO getHerbByRowNum(Integer rowNum) {
        if (rowNum == null || rowNum < 2) {
            throw new IllegalArgumentException("유효하지 않은 행 번호입니다.");
        }
        ValueRange result;
        try {
            String range = SheetsInfo.HERB.getSpecificRowRange(rowNum);
            result = herbRepository.selectByRange(range);
            List<HerbDTO> herbDTOList = herbMapper.toHerbDTOList(result);
            if (herbDTOList.isEmpty()) {
                throw new GoogleSpreadsheetsAPIException("해당 행 번호에 약재 정보가 존재하지 않습니다.");
            }
            return herbDTOList.get(0);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error fetching herb data for row {}: {}", rowNum, e.getMessage());
            throw new GoogleSpreadsheetsAPIException("약재 재고 정보를 불러오는 데 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    /**
     * 약재 등록
     * 
     * @param herbRegisterDTO 등록할 약재 정보
     */
    public void insertHerb(HerbRegisterDTO herbRegisterDTO) {
        transactionalInsertHerb(herbRegisterDTO);
    }

    /**
     * 약재 등록 트랜잭션 처리
     * 약재 등록 -> 로그 생성 순으로 처리하며, 중간에 실패할 경우 롤백 수행.
     * 
     * @param herbRegisterDTO 등록할 약재 정보
     */
    private void transactionalInsertHerb(HerbRegisterDTO herbRegisterDTO) {
        /*
            1. 약재 정보를 스프레드시트에 반영
         */
        // 약재 등록 후 삽입된 범위 정보 -> 롤백 시 사용
        String herbInsertedRange;
        try {
            herbInsertedRange = doInsertHerb(herbRegisterDTO);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error inserting herb data for {}: {}", herbRegisterDTO.getName(), e.getMessage());
            throw new GoogleSpreadsheetsAPIException("약재 등록에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
        
        /*
            2. 약재 등록 내역을 로그 시트에 기록
         */
        try {
            logInsertHerb(herbRegisterDTO);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error logging inserted herb data for {}: {}", herbRegisterDTO.getName(), e.getMessage());
            log.warn("Attempting to rollback herb insert for {}", herbRegisterDTO.getName());

            // 약재 등록 롤백 시도
            try {
                rollbackHerbInsert(herbInsertedRange);
            } catch (GeneralSecurityException | IOException e1) {
                // 롤백 실패
                log.error("[CRITICAL] Inserting Rollback failed for {}: {}", herbRegisterDTO.getName(), e1.getMessage());
                throw new RollbackFailedException(
                        "약재 등록에 실패하여 데이터 자동 복구를 시도하였으나 실패했습니다.\n약재 재고 수량 변화가 정상적으로 등록되지 않았을 수 있습니다.", e1
                );
            }
            
            // 롤백 성공
            log.info("Inserting Rollback successful for {}", herbRegisterDTO.getName());
            throw new GoogleSpreadsheetsAPIException("약재 등록에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    /**
     * 약재 정보 삽입 수행
     * 
     * @param herbRegisterDTO 등록할 약재 정보
     * @return 삽입된 범위 문자열
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    private String doInsertHerb(HerbRegisterDTO herbRegisterDTO) throws GeneralSecurityException, IOException {
        List<List<Object>> value = herbMapper.fromHerbRegisterDTO(herbRegisterDTO);
        return herbRepository.insertHerb(value);
    }

    /**
     * 약재 등록 로그 기록
     * 
     * @param dto 등록된 약재 정보
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    private void logInsertHerb(HerbRegisterDTO dto) throws GeneralSecurityException, IOException {
        HerbLogDTO logDTO = HerbLogDTO.builder()
                .loggedDatetime(LocalDateTime.now())
                .name(dto.getName())
                .beforeAmount(0L)
                .afterAmount(dto.getAmount())
                .build();
        List<List<Object>> value = herbMapper.fromHerbLogDTO(logDTO);

        herbLogRepository.insertLog(value);
    }

    /**
     * 약재 등록 롤백 수행
     * 
     * @param rollbackRange 롤백할 범위 문자열
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    private void rollbackHerbInsert(String rollbackRange) throws GeneralSecurityException, IOException {
        herbRepository.deleteByRange(rollbackRange);
    }

    /**
     * 약재 재고 및 메모 수정
     *
     * @param updateDTOList 수정할 약재 정보 리스트
     */
    public void updateHerbs(List<HerbUpdateDTO> updateDTOList) {
        for (HerbUpdateDTO dto : updateDTOList) {
            transactionalUpdateHerb(dto);
        }
    }

    /**
     * 약재 재고 및 메모 수정 트랜잭션 처리
     * 약재 정보 수정 -> 로그 생성 순으로 처리하며, 중간에 실패할 경우 롤백 수행.
     *
     * @param dto 수정할 약재 정보
     */
    private void transactionalUpdateHerb(HerbUpdateDTO dto) {
        /*
            1. 수정 사항을 스프레드시트에 반영
         */
        // 약재 수정 후 수정된 범위 정보 -> 롤백 시 사용
        String updatedRange;
        try {
            updatedRange = updateHerbWithOptimisticLocking(dto);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error updating herb data for {}: {}", dto.getName(), e.getMessage());
            throw new GoogleSpreadsheetsAPIException("재고 또는 메모 수정에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }

        if (!dto.isAmountChanged()) {
            // 수량 변경이 없는 경우 로그 기록 생략
            return;
        }

        /*
            2. 수정 내역을 로그 시트에 기록
         */
        try {
            logUpdateHerb(dto);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error logging updated herb data for {}: {}", dto.getName(), e.getMessage());
            log.warn("Attempting to rollback herb update for {}", dto.getName());

            // 약재 재고 수정 롤백 시도
            try {
                rollbackHerbUpdate(updatedRange, dto);
            } catch (GeneralSecurityException | IOException e1) {
                // 롤백 실패
                log.error("[CRITICAL] Updating Rollback failed for {}: {}", dto.getName(), e1.getMessage());
                throw new RollbackFailedException("재고 수정에 실패하여 데이터 자동 복구를 시도하였으나 실패했습니다.\n수동 복구가 필요합니다.", e1);
            }

            // 롤백 성공
            log.info("Updating Rollback successful for {}", dto.getName());
            throw new GoogleSpreadsheetsAPIException("재고 수정에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    /**
     * 낙관적 잠금을 이용한 약재 정보 수정.<br/>
     * 기존 약재 정보와 수정 전 약재 정보를 비교하여 동일할 경우에만 수정 수행.
     * 그렇지 않은 경우 {@link OptimisticLockingException} 예외 발생.
     *
     * @param dto 수정할 약재 정보
     * @return 수정된 범위 문자열
     * @throws GeneralSecurityException on security exception.
     * @throws IOException on Credentials file read exception.
     */
    private String updateHerbWithOptimisticLocking(HerbUpdateDTO dto) throws GeneralSecurityException, IOException {
        HerbDTO expectedHerbDTO = HerbDTO.from(dto),
                actualHerbDTO = getHerbByRowNum(dto.getRowNum());
        if (expectedHerbDTO != null && expectedHerbDTO.equals(actualHerbDTO)) {
            return doUpdateHerb(dto);
        } else {
            throw new OptimisticLockingException("재고 수정에 실패했습니다.\n다른 사용자가 해당 약재 정보를 수정했을 수 있습니다. 최신 정보를 불러온 후 다시 시도해주세요.");
        }
    }

    /**
     * 약재 정보 수정 수행
     *
     * @param dto 수정할 약재 정보
     * @return 수정된 범위 문자열
     */
    private String doUpdateHerb(HerbUpdateDTO dto) throws GeneralSecurityException, IOException {
        String range = SheetsInfo.HERB.getSpecificRowRange(dto.getRowNum());
        List<List<Object>> value = herbMapper.fromHerbUpdateDTOForUpdate(dto);
        return herbRepository.updateByRange(range, value);
    }

    /**
     * 약재 정보 수정 로그 기록
     *
     * @param dto 수정된 약재 정보
     */
    private void logUpdateHerb(HerbUpdateDTO dto) throws GeneralSecurityException, IOException {
        HerbLogDTO logDTO = HerbLogDTO.builder()
                .loggedDatetime(LocalDateTime.now())
                .name(dto.getName())
                .beforeAmount(dto.getOriginalAmount())
                .afterAmount(dto.getNewAmount())
                .build();
        List<List<Object>> value = herbMapper.fromHerbLogDTO(logDTO);

        herbLogRepository.insertLog(value);
    }

    /**
     * 약재 정보 수정 롤백 수행
     *
     * @param dto 수정된 약재 정보
     */
    private void rollbackHerbUpdate(String rollbackRange, HerbUpdateDTO dto) throws GeneralSecurityException, IOException {
        List<List<Object>> value = herbMapper.fromHerbUpdateDTOForRollback(dto);
        herbRepository.updateByRange(rollbackRange, value);
    }

    /**
     * 약재 수정 로그 시트의 모든 행을 조회.
     *
     * @return 스프레드시트의 모든 행 정보 {@link ValueRange}.
     */
    public List<HerbLogViewDTO> getAllHerbLogs() {
        ValueRange result;
        try {
            result = herbLogRepository.selectAll();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error fetching herb data: {}", e.getMessage());
            throw new GoogleSpreadsheetsAPIException("약재 재고 정보를 불러오는 데 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
        List<HerbLogDTO> herbLogDTOList = herbMapper.toHerbLogDTOList(result);

        return HerbLogViewDTO.from(herbLogDTOList);
    }


}