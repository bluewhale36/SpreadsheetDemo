package com.example.spreadsheetdemo.herb.service;

import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.exception.OptimisticLockingException;
import com.example.spreadsheetdemo.common.exception.RollbackFailedException;
import com.example.spreadsheetdemo.herb.domain.entity.Herb;
import com.example.spreadsheetdemo.herb.domain.entity.HerbLog;
import com.example.spreadsheetdemo.herb.domain.model.HerbStatisticsModel;
import com.example.spreadsheetdemo.herb.dto.*;
import com.example.spreadsheetdemo.herb.repository.HerbLogRepository;
import com.example.spreadsheetdemo.herb.repository.HerbRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class HerbService {

    private final HerbRepository herbRepository;
    private final HerbLogRepository herbLogRepository;


    /**
     * 약재 정보를 조회.<br/>
     * {@code keyword} 가 전달되었을 경우 해당 문자열을 포함하는 이름을 가진 약재 정보가 조회되며, {@code null} 값 또는 빈 문자열이 전달되었을 경우 모든 약재 정보가 조회됨.
     *
     * @param keyword 약재 이름에 포함되는 검색 키워드.
     * @return 약재 정보를 저장하는 {@link HerbDTO} 객체의 리스트.
     */
    public List<HerbDTO> getHerbs(String keyword) {
        try {
            List<Herb> entityList;
            if (keyword == null || keyword.isBlank()) {
                entityList = herbRepository.findAll();
            } else {
                entityList = herbRepository.findAllByNameContains(keyword).orElse(List.of());
            }
            return entityList.stream().map(HerbDTO::from).toList();
        } catch (GoogleSpreadsheetsAPIException e) {
            throw new GoogleSpreadsheetsAPIException("약재 정보를 불러올 수 없습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    /**
     * 특정 약재의 정보와 모든 재고 변화 기록을 조회.
     *
     * @param name 조회할 약재의 이름
     * @return 약재의 정보와 재고 변화 기록을 저장하는 {@link HerbInfoDTO} 객체.
     */
    public HerbInfoDTO getOneHerbInfo(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name should not be null while getting Herb.");

        try {
            Herb entity = herbRepository.findAllByName(name)
                    .orElseThrow(() -> new IllegalArgumentException(name + " 의 약재 정보를 찾을 수 없습니다."))
                    .get(0);
            HerbDTO herbDTO = HerbDTO.from(entity);
            List<HerbLogDTO> herbLogDTOList = herbLogRepository.findAllByName(name)
                    .orElse(List.of())
                    .stream()
                    .map(HerbLogDTO::from)
                    .toList();
            return HerbInfoDTO.of(herbDTO, herbLogDTOList);
        } catch (GoogleSpreadsheetsAPIException e) {
            throw new GoogleSpreadsheetsAPIException("약재 정보를 불러올 수 없습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    public Set<String> getAllHerbName() {
        List<String> nameList = herbRepository.findAllName().orElse(List.of());
        return new HashSet<>(nameList);
    }

    /**
     * 약재 등록
     * 
     * @param herbRegisterDTO 등록할 약재 정보
     */
    public void insertHerb(HerbRegisterDTO herbRegisterDTO) {
        if (herbRegisterDTO == null) throw new IllegalArgumentException("HerbRegisterDTO should not be null while inserting new Herb.");
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
    private Herb doInsertHerb(HerbRegisterDTO herbRegisterDTO) throws GoogleSpreadsheetsAPIException {
        Herb insertingEntity = Herb.create(herbRegisterDTO);
        return herbRepository.save(insertingEntity);
    }

    /**
     * 약재 등록 로그 기록
     * 
     * @param dto 등록된 약재 정보
     */
    private void logInsertHerb(HerbRegisterDTO dto) throws GoogleSpreadsheetsAPIException {
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
        if (updateDTOList == null || updateDTOList.isEmpty()) return;
        for (HerbUpdateDTO dto : updateDTOList) {
            if (dto.getRowNum() == null) {
                throw new IllegalArgumentException("rowNum of HerbUpdateDTO should not be null while updating Herb.");
            }
        }
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
    private List<Herb> updateHerbWithOptimisticLocking(List<HerbUpdateDTO> dtoList) throws GoogleSpreadsheetsAPIException {
        List<HerbDTO> expectedHerbDTOList = dtoList.stream().map(HerbDTO::from).sorted(Comparator.comparing(HerbDTO::getRowNum)).toList(), actualHerbDTOList;
        List<Herb> entityList = herbRepository
                        .findAllByRowNums(
                                dtoList.stream().map(HerbUpdateDTO::getRowNum).collect(Collectors.toSet())
                        )
                        .orElse(List.of());
        actualHerbDTOList = entityList.stream().map(HerbDTO::from).sorted(Comparator.comparing(HerbDTO::getRowNum)).toList();

        if (Objects.equals(expectedHerbDTOList, actualHerbDTOList)) {
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
    private List<Herb> doUpdateHerb(List<HerbUpdateDTO> dtoList) throws GoogleSpreadsheetsAPIException {
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
    private void logUpdateHerb(List<HerbUpdateDTO> dtoList) throws GoogleSpreadsheetsAPIException {
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
    private void rollbackHerbUpdate(List<HerbUpdateDTO> dtoList) throws GoogleSpreadsheetsAPIException {
        List<Herb> rollingBackEntityList = dtoList.stream()
                        .map(
                                dto -> Herb.of(dto.getRowNum(), dto.getName(), dto.getOriginalAmount(), dto.getOriginalLastStoredDate(), dto.getOriginalMemo())
                        )
                        .toList();
        herbRepository.saveAll(rollingBackEntityList);
    }

    public void hardDeleteOneHerb(HerbDTO deletingHerbDTO) {
        if (deletingHerbDTO == null) throw new IllegalArgumentException("HerbDTO should not be null for deletion.");

        transactionalHardDeleteOneHerb(deletingHerbDTO);
    }

    /**
     * 약재 및 재고 기록 삭제 트랜잭션 처리
     * 약재 삭제 -> 로그 삭제 순으로 처리하며, 중간에 실패할 경우 롤백 수행.
     *
     * @param deletingHerbDTO 삭제할 약재 정보
     */
    private void transactionalHardDeleteOneHerb(HerbDTO deletingHerbDTO) {
        try {
            hardDeleteWithOptimisticLocking(deletingHerbDTO);
        } catch (GoogleSpreadsheetsAPIException e) {
            throw new GoogleSpreadsheetsAPIException("약재 삭제에 실패했습니다.\n잠시 뒤 다시 시도해주세요.", e);
        }

        try {
            deleteLogsForOneHerb(deletingHerbDTO);
        } catch (GoogleSpreadsheetsAPIException e) {
            log.error("Error deleting herb log data : {}", e.getMessage());
            log.warn("Attempting to rollback herb deletion...");

            try {
                rollbackHerbDeletion(deletingHerbDTO);
            } catch (GoogleSpreadsheetsAPIException e1) {
                // 롤백 실패
                log.error("[CRITICAL] Deletion Rollback failed : {}", e1.getMessage());
                throw new RollbackFailedException("약재 삭제에 실패하여 데이터 자동 복구를 시도하였으나 실패했습니다.\n수동 복구가 필요합니다.", e1);
            }

            // 롤백 성공
            log.info("Deletion Rollback successful");
            throw new GoogleSpreadsheetsAPIException("약재 삭제에 실패했습니다. 잠시 뒤 다시 시도해주세요.", e);
        }
    }

    /**
     * 낙관적 잠금을 이용한 약재 삭제.<br/>
     * 삭제할 약재 정보와 현재 Spreadsheet 에 기록된 약재 정보를 행 번호를 기준으로 조회하고 비교하여 동일할 경우에만 삭제 수행.
     * 그렇지 않은 경우 {@link OptimisticLockingException} 예외 발생.
     *
     * @param deletingHerbDTO 삭제할 약재 정보.
     */
    private void hardDeleteWithOptimisticLocking(HerbDTO deletingHerbDTO) throws GoogleSpreadsheetsAPIException {
        HerbDTO actualHerb = HerbDTO.from(herbRepository.findByRowNum(deletingHerbDTO.getRowNum()).orElse(null));
        if (!Objects.equals(deletingHerbDTO, actualHerb)) {
            throw new OptimisticLockingException("약재 삭제에 실패했습니다.\n다른 사용자가 해당 약재 정보를 수정했을 수 있습니다. 최신 정보를 불러온 후 다시 시도해주세요.");
        }
        herbRepository.deleteByRowNum(deletingHerbDTO.getRowNum());
    }

    /**
     * 약재의 재고 기록 전체 삭제.
     *
     * @param herbDTO 삭제할 재고 기록의 약재 정보
     */
    private void deleteLogsForOneHerb(HerbDTO herbDTO) throws GoogleSpreadsheetsAPIException {
        herbLogRepository.deleteAllByName(herbDTO.getName());
    }

    /**
     * 약재 삭제 롤백 수행.
     *
     * @param rollingBackDTO 롤백할 약재 정보.
     */
    private void rollbackHerbDeletion(HerbDTO rollingBackDTO) throws GoogleSpreadsheetsAPIException {
        HerbRegisterDTO restore = HerbRegisterDTO.builder()
                .name(rollingBackDTO.getName())
                .amount(rollingBackDTO.getAmount())
                .lastStoredDate(rollingBackDTO.getLastStoredDate())
                .memo(rollingBackDTO.getMemo())
                .build();
        herbRepository.save(Herb.create(restore));
    }

    /**
     * 특정 일자 내의 모든 약재 재고 변화 기록을 조히. 일자가 명시되지 않은 경우 실행 당일을 기준으로 1개월 이내의 기록이 조회됨.
     *
     * @param from 기록을 조회하는 일자 범위의 시작 일자. {@code null} 값 전달 시 {@code to} 매개변수의 1개월 전 일자를 기준으로 함.
     * @param to 기록을 조회하는 일자 범위의 종료 일자. {@code null} 값 전달 시 실행 일자를 기준으로 함.
     * @return 전달된 일자 범위 내의 모든 약재 재고 변화 기록을 담은 {@link HerbLogViewDTO} 의 리스트.
     */
    public List<HerbLogViewDTO> getHerbLogs(LocalDate from, LocalDate to) {
        LocalDate
                toInclude = to == null ? LocalDate.now() : to,
                fromExclude = from == null ? toInclude.minusMonths(1) : from.minusDays(1);

        List<HerbLog> entityList = herbLogRepository.findAllByLoggedDateTimeBetween(fromExclude, toInclude).orElse(List.of());

        return HerbLogViewDTO.from(entityList.stream().map(HerbLogDTO::from).toList());
    }

    /**
     * 특정 일자 내의 모든 약재 재고 변화 기록에 대한 통계 모델을 조회. 일자가 명시되지 않은 경우 실행 당일을 기준으로 1개월 이내의 기록에 대한 통계가 조회됨.
     *
     * @param from 통계를 위한 기록을 조회하는 일자 범위의 시작 일자. {@code null} 값 전달 시 {@code to} 매개변수의 1개월 전 일자를 기준으로 함.
     * @param to 통계를 위한 기록을 조회하는 일자 범위의 종료 일자. {@code null} 값 전달 시 실행 일자를 기준으로 함.
     * @return 전달된 일자 범위 내의 모든 약재 재고 변화 기록에 대한 통계 모델인 {@link HerbStatisticsModel} 객체.
     */
    public HerbStatisticsModel getHerbLogStatistics(LocalDate from, LocalDate to) {
        LocalDate
                toInclude = to == null ? LocalDate.now() : to,
                fromExclude = from == null ? toInclude.minusMonths(1) : from.minusDays(1);

        List<HerbDTO> herbDTOList = herbRepository.findAll().stream().map(HerbDTO::from).toList();
        List<HerbLogDTO> herbLogDTOList = herbLogRepository.findAllByLoggedDateTimeBetween(fromExclude, toInclude).orElse(List.of()).stream().map(HerbLogDTO::from).toList();

        Map<HerbDTO, List<HerbLogDTO>> listMap = new HashMap<>();
        for (HerbDTO herbDTO : herbDTOList) {
            listMap.put(
                    herbDTO,
                    herbLogDTOList.stream()
                            .filter(log -> log.getName().equals(herbDTO.getName()))
                            .sorted(Comparator.comparing(HerbLogDTO::getLoggedDateTime).reversed())
                            .toList()
            );
        }

        return HerbStatisticsModel.of(fromExclude.plusDays(1), toInclude, listMap);
    }


}