package com.example.spreadsheetdemo.herb.service;

import com.example.spreadsheetdemo.common.enums.SheetsInfo;
import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.exception.OptimisticLockingException;
import com.example.spreadsheetdemo.common.exception.RollbackFailedException;
import com.example.spreadsheetdemo.herb.domain.HerbLogPagination;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import com.example.spreadsheetdemo.herb.dto.*;
import com.example.spreadsheetdemo.herb.repository.HerbLogRepository;
import com.example.spreadsheetdemo.herb.repository.HerbRepository;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class HerbService {

    private final HerbRepository herbRepository;
    private final HerbLogRepository herbLogRepository;


    /**
     * 약재 정보가 담긴 스프레드시트의 모든 행을 조회.
     *
     * @return 스프레드시트의 모든 행 정보 {@link ValueRange}.
     */
    public List<HerbDTO> getAllHerbs() {
        List<Herb> entityList = herbRepository.findAll();
        return entityList.stream().map(HerbDTO::from).toList();
    }

    public HerbDTO getHerbByRowNum(Integer rowNum) {
        Herb entity = herbRepository.findByRowNum(rowNum);
        return HerbDTO.from(entity);
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
        Herb insertedEntity;
        try {
            insertedEntity = doInsertHerb(herbRegisterDTO);
        } catch (GoogleSpreadsheetsAPIException e) {
            throw new GoogleSpreadsheetsAPIException("약재 등록에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
        
        /*
            2. 약재 등록 내역을 로그 시트에 기록
         */
        try {
            logInsertHerb(herbRegisterDTO);
        } catch (GoogleSpreadsheetsAPIException e) {
            log.error("Error logging inserted herb data for {}: {}", herbRegisterDTO.getName(), e.getMessage());
            log.warn("Attempting to rollback herb insert for {}", herbRegisterDTO.getName());

            // 약재 등록 롤백 시도
            try {
                rollbackHerbInsert(insertedEntity);
            } catch (GoogleSpreadsheetsAPIException e1) {
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
     * @param herbRegisterDTO 등록할 약재 정보.
     * @return 삽입된 약재 정보 {@link Herb} 객체.
     */
    private Herb doInsertHerb(HerbRegisterDTO herbRegisterDTO) {
        Herb insertingEntity = Herb.create(herbRegisterDTO);
        return herbRepository.save(insertingEntity);
    }

    /**
     * 약재 등록 로그 기록
     * 
     * @param dto 등록된 약재 정보
     */
    private void logInsertHerb(HerbRegisterDTO dto) {
        HerbLog insertingEntity = HerbLog.of(
                null, LocalDateTime.now(), dto.getName(), 0L, dto.getAmount()
        );
        herbLogRepository.save(insertingEntity);
    }

    /**
     * 약재 등록 롤백 수행
     * 
     * @param rollingBackEntity 롤백할 {@link Herb} 객체.
     */
    private void rollbackHerbInsert(Herb rollingBackEntity) {
        herbRepository.deleteByRowNum(rollingBackEntity.getRowNum());
    }

    /**
     * 약재 재고 및 메모 수정
     *
     * @param updateDTOList 수정할 약재 정보 리스트
     */
    public void updateHerbs(List<HerbUpdateDTO> updateDTOList) {
        transactionalUpdateHerb(updateDTOList);
    }

    /**
     * 약재 재고 및 메모 수정 트랜잭션 처리
     * 약재 정보 수정 -> 로그 생성 순으로 처리하며, 중간에 실패할 경우 롤백 수행.
     *
     * @param dtoList 수정할 약재 정보
     */
    private void transactionalUpdateHerb(List<HerbUpdateDTO> dtoList) {
        /*
            1. 수정 사항을 스프레드시트에 반영
         */
        // 약재 수정 후 수정된 범위 정보 -> 롤백 시 사용
        List <Herb> updatedEntityList;
        try {
            updatedEntityList = updateHerbWithOptimisticLocking(dtoList);
        } catch (GoogleSpreadsheetsAPIException e) {
            log.error("Error updating herb data : {}", e.getMessage());
            throw new GoogleSpreadsheetsAPIException("재고 또는 메모 수정에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }

        List<HerbUpdateDTO> loggingDTOList = dtoList.stream().filter(HerbUpdateDTO::isAmountChanged).toList();
        if (loggingDTOList.isEmpty()) {
            // 수량 변경이 없는 경우 로그 기록 생략
            return;
        }

        /*
            2. 수정 내역을 로그 시트에 기록
         */
        try {
            logUpdateHerb(loggingDTOList);
        } catch (GoogleSpreadsheetsAPIException e) {
            log.error("Error logging updated herb data : {}", e.getMessage());
            log.warn("Attempting to rollback herb update...");

            // 약재 재고 수정 롤백 시도
            try {
                rollbackHerbUpdate(dtoList);
            } catch (GoogleSpreadsheetsAPIException e1) {
                // 롤백 실패
                log.error("[CRITICAL] Updating Rollback failed : {}", e1.getMessage());
                throw new RollbackFailedException("재고 수정에 실패하여 데이터 자동 복구를 시도하였으나 실패했습니다.\n수동 복구가 필요합니다.", e1);
            }

            // 롤백 성공
            log.info("Updating Rollback successful");
            throw new GoogleSpreadsheetsAPIException("재고 수정에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    /**
     * 낙관적 잠금을 이용한 약재 정보 수정.<br/>
     * 기존 약재 정보와 수정 전 약재 정보를 비교하여 동일할 경우에만 수정 수행.
     * 그렇지 않은 경우 {@link OptimisticLockingException} 예외 발생.
     *
     * @param dtoList 수정할 약재 정보 리스트
     * @return 수정된 약재 정보 리스트
     */
    private List<Herb> updateHerbWithOptimisticLocking(List<HerbUpdateDTO> dtoList) {
        List<HerbDTO> expectedHerbDTOList = dtoList.stream().map(HerbDTO::from).toList(), actualHerbDTOList;
        List<Herb> entityList = herbRepository
                        .findAllByRowNums(
                                dtoList.stream().map(HerbUpdateDTO::getRowNum).collect(Collectors.toSet())
                        )
                        .orElse(List.of());
        actualHerbDTOList = entityList.stream().map(HerbDTO::from).toList();

        if (expectedHerbDTOList.equals(actualHerbDTOList)) {
            return doUpdateHerb(dtoList);
        } else {
            throw new OptimisticLockingException("재고 수정에 실패했습니다.\n다른 사용자가 해당 약재 정보를 수정했을 수 있습니다. 최신 정보를 불러온 후 다시 시도해주세요.");
        }
    }

    /**
     * 약재 정보 수정 수행
     *
     * @param dtoList 수정할 약재 정보 리스트
     * @return 수정된 약재 정보 리스트
     */
    private List<Herb> doUpdateHerb(List<HerbUpdateDTO> dtoList) {
        List<Herb> updatingEntityList = dtoList.stream()
                .map(
                        dto -> Herb.of(dto.getRowNum(), dto.getName(), dto.getNewAmount(), dto.getNewLastStoredDate(), dto.getNewMemo())
                )
                .toList();
        return herbRepository.saveAll(updatingEntityList);
    }

    /**
     * 약재 정보 수정 로그 기록
     *
     * @param dtoList 수정된 약재 정보 리스트
     */
    private void logUpdateHerb(List<HerbUpdateDTO> dtoList) {
        List<HerbLog> insertingEntityList = dtoList.stream()
                .map(
                        dto -> HerbLog.of(null, LocalDateTime.now(), dto.getName(), dto.getOriginalAmount(), dto.getNewAmount())
                )
                .toList();
        herbLogRepository.saveAll(insertingEntityList);
    }

    /**
     * 약재 정보 수정 롤백 수행
     *
     * @param dtoList 수정된 약재 정보 리스트
     */
    private void rollbackHerbUpdate(List<HerbUpdateDTO> dtoList) {
        List<Herb> rollingBackEntityList = dtoList.stream()
                        .map(
                                dto -> Herb.of(dto.getRowNum(), dto.getName(), dto.getOriginalAmount(), dto.getOriginalLastStoredDate(), dto.getOriginalMemo())
                        )
                        .toList();
        herbRepository.saveAll(rollingBackEntityList);
    }

    /**
     * 약재 수정 로그 시트의 페이징 처리된 행을 조회.
     *
     * @return 해당 페이지의 로그 정보를 담은 리스트.
     */
    public HerbLogPagination getHerbLogs(LocalDate stdDate) {
        try {

            LocalDate toInclude = stdDate == null ? LocalDate.now() : stdDate, fromExclude = toInclude.minusMonths(1);

            /*
                1. endRowNum 계산
             */
            int endRowNum = getEndRowNumForHerbLogPagination(fromExclude, toInclude);

            /*
                2. startRowNum 계산
             */
            int startRowNum = getStartRowNumForHerbLogPagination(fromExclude, toInclude, endRowNum);

            /*
                3. 해당 범위의 로그 데이터 조회
             */
            List<HerbLog> entityList = herbLogRepository.findAllByRowNumRange(startRowNum, endRowNum).orElse(List.of());
            // 변환 및 반환
            List<HerbLogDTO> herbLogDTOList = entityList.stream().map(HerbLogDTO::from).toList();

            return HerbLogPagination.of(HerbLogViewDTO.from(herbLogDTOList), startRowNum, endRowNum, toInclude, fromExclude);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error fetching herb log data: {}", e.getMessage());
            throw new GoogleSpreadsheetsAPIException("약재 재고 로그 정보를 불러오는 데 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    // 한번에 조회할 행 단위
    int chunkSize = 500;

    private int getEndRowNumForHerbLogPagination(LocalDate fromExclude, LocalDate toInclude) throws GeneralSecurityException, IOException {
        int endRowNum;
        // 역순으로 chunkSize 만큼 일자 조회하면서 toInclude 일자가 포함된 마지막 행 번호 계산
        int tmpEndRowNum = herbLogRepository.countAll() + (SheetsInfo.HERB_LOG.getStartRowNum() -1);
        System.out.println("\n\ntmpEndRowNum : " + tmpEndRowNum + "\n\n");
        for (int i = 1; ; i++) {

            if (i > 1)  tmpEndRowNum = Math.max(tmpEndRowNum - chunkSize, 2);

            // 로그 일자 조회
            List<LocalDateTime> loggedDateTimeList = herbLogRepository.findAllLoggedDateTimeByRowNumRange(tmpEndRowNum - chunkSize +1, tmpEndRowNum).orElse(List.of());

            List<LocalDate> loggedDateList = loggedDateTimeList.stream().map(LocalDate::from).toList();

            // toInclude 일자와 fromExclude 일자 사이에 있는 로그 일자만 필터링
            List<LocalDate> filteredLoggedDateList = loggedDateList.stream()
                    .filter(
                            date -> (
                                    (date.isBefore(toInclude) || date.isEqual(toInclude)) && date.isAfter(fromExclude)
                            )
                    )
                    .toList();

            if (!filteredLoggedDateList.isEmpty()) {
                // toInclude 포함 이전 일자 또는 fromExclude 이후 일자 중 최신 일자 조회
                LocalDate validToInclude = filteredLoggedDateList.stream().max(LocalDate::compareTo).get();

                int lastIndex = loggedDateList.lastIndexOf(validToInclude);

                if (lastIndex != -1) {
                    // 포함할 마지막 일자가 조회된 경우 해당 인덱스를 기준으로 endRowNum 계산
                    endRowNum = tmpEndRowNum -( loggedDateList.size() -1 - lastIndex );
                    break;
                } else if (tmpEndRowNum == 2) {
                    endRowNum = tmpEndRowNum;
                    break;
                }
            }
        }
        return endRowNum;
    }

    private int getStartRowNumForHerbLogPagination(LocalDate fromExclude, LocalDate toInclude, int endRowNum) throws GeneralSecurityException, IOException {
        int startRowNum;
        int tmpStartRowNum;
        for (int i = 1; ; i++) {

            tmpStartRowNum = Math.max(endRowNum - i*chunkSize +1, 2);

            // 로그 일자 조회
            List<LocalDateTime> loggedDateTimeList = herbLogRepository.findAllLoggedDateTimeByRowNumRange(tmpStartRowNum, endRowNum).orElse(List.of());
            List<LocalDate> loggedDateList = loggedDateTimeList.stream().map(LocalDate::from).toList();
            // toInclude 일자와 fromExclude 일자 사이에 있는 로그 일자만 필터링
            List<LocalDate> filteredLoggedDateList = loggedDateList.stream()
                    .filter(
                            date -> (
                                    (date.isBefore(toInclude) || date.isEqual(toInclude)) && date.isAfter(fromExclude)
                            )
                    )
                    .toList();

            if (loggedDateList.size() != filteredLoggedDateList.size()) {
                // 두 리스트의 개수가 다를 경우 범위 내에 포함되지 않는 일자가 존재함
                // -> 해당 일자 내 데이터가 모두 조회되므로 startRowNum 계산
                LocalDate validFromExclude = filteredLoggedDateList.stream().min(LocalDate::compareTo).get();

                int firstIndex = loggedDateList.indexOf(validFromExclude);
                startRowNum = tmpStartRowNum + firstIndex;
                break;
            } else if (tmpStartRowNum == 2) {
                // 시작 행 번호가 2인 경우 더 이상 범위를 넓힐 수 없음 -> 전체 범위 조회
                startRowNum = tmpStartRowNum;
                break;
            }
        }
        return startRowNum;
    }

}