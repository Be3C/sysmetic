package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.strategy.dto.DailyTransactionInputDataDto;
import com.be3c.sysmetic.domain.strategy.entity.Daily;
import com.be3c.sysmetic.domain.strategy.entity.Monthly;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.repository.DailyRepository;
import com.be3c.sysmetic.domain.strategy.repository.MonthlyRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import com.be3c.sysmetic.domain.strategy.util.DoubleHandler;
import com.be3c.sysmetic.domain.strategy.util.StrategyCalculator;
import com.be3c.sysmetic.global.util.file.exception.InvalidFileFormatException;
import com.be3c.sysmetic.global.util.file.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    final DailyRepository dailyRepository;
    final MonthlyRepository monthlyRepository;
    final MonthlyService monthlyService;
    final StrategyRepository strategyRepository;
    final StrategyDetailService strategyDetailService;
    final DoubleHandler doubleHandler;
    final StrategyCalculator strategyCalculator;
    final S3Service s3Service;

    @Value("${daily.excel.form.path}")
    private String excelFormPath;

    @Override
    public String getExcelFormUrl(){
        return s3Service.createPresignedGetUrl(excelFormPath+"(양식)_일간분석_데이터_입력.xlsx");
    }

    /**
     * 엑셀 파일을 읽어서 일간 데이터를 DB에 저장
     * @param file 규정된 컬럼 형식의 엑셀 파일
     * @param strategyId 일간 데이터는 전략id 해당 전략에 대한 데이터
     */
    @Transactional
    @Override
    public void uploadExcel(MultipartFile file, Long strategyId) {

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("전략을 찾지 못했습니다 : " + strategyId));

        if (!isValidExcelFile(file)) {
            throw new InvalidFileFormatException("엑셀 업로드 실패: 파일 형식이 올바르지 않습니다.");
        }

//        int batchSize = 500; // 배치 작업 - 2차개발 추가
//        List<DailyTransactionDataDto> newDtos = new ArrayList<>(batchSize);

        /* 엑셀 to DTO */
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);

            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidFileFormatException("엑셀 파일에 시트가 없습니다.");
            }
            Sheet sheet = workbook.getSheetAt(0);   // 첫번째 시트만 읽음

            List<DailyTransactionInputDataDto> excelData = new ArrayList<>(sheet.getPhysicalNumberOfRows());

            for (Row row : sheet) {
                if (row == null || row.getRowNum() == 0) continue;  // 헤더

                boolean isValid = true;

                for(int i = 0; i<3; i++){
                    if (row.getCell(i) == null
                            || row.getCell(i).toString().trim().isEmpty()
                            || row.getCell(i).getCellType()!= CellType.NUMERIC) {   // 날짜, 숫자, 숫자
                        isValid = false;
                        break;
                    }
                }

                if(!isValid)
                    continue;

                // 셀 읽기
                Date firstColumn = row.getCell(0).getDateCellValue();
                Double secondColumn = row.getCell(1).getNumericCellValue();
                Double thirdColumn = row.getCell(2).getNumericCellValue();

                LocalDate date = firstColumn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (date.isAfter(LocalDate.now())) {
                    throw new InvalidFileFormatException("엑셀 업로드 실패: 미래의 데이터는 입력할 수 없습니다.");
                }
                Double depositWithdrawalAmount = doubleHandler.cutDouble(secondColumn);
                Double profitLossAmount = doubleHandler.cutDouble(thirdColumn);

                excelData.add(new DailyTransactionInputDataDto(date, depositWithdrawalAmount, profitLossAmount));
            }

            // 저장하기
            if(!excelData.isEmpty()){
                /* 날짜 순 정렬 보장하기 */
                boolean isSorted = true;
                for (int i = 1; i < excelData.size(); i++) {
                    if (excelData.get(i).date().isBefore(excelData.get(i - 1).date())) {
                        isSorted = false;
                        break;
                    }
                }
                if (!isSorted) {
                    excelData.sort(Comparator.comparing(DailyTransactionInputDataDto::date));
                }


                /* 비교해서 save */
                List<Daily> saveTargets = new ArrayList<>();
                List<Daily> existingEntities = dailyRepository.findByDateGreaterThanEqualAndStrategyOrderByDateAsc(excelData.get(0).date(), strategy);

                for (DailyTransactionInputDataDto excelDailyDto : excelData) {

                    LocalDate newDailyDate = excelDailyDto.date();

                    int existingIndex = Collections.binarySearch(existingEntities, excelDailyDto.toEntity(strategy));

                    if (existingIndex < 0) {    /* Add completely new data */

                        Daily daily = Daily.builder()
                                .strategy(strategy)
                                .date(newDailyDate)
                                .depositWithdrawalAmount(excelDailyDto.depositWithdrawalAmount())
                                .profitLossAmount(excelDailyDto.profitLossAmount())

                                .principal(0.0)
                                .currentBalance(0.0)
                                .standardAmount(0.0)
                                .profitLossRate(0.0)
                                .accumulatedProfitLossAmount(0.0)
                                .accumulatedProfitLossRate(0.0)
                                .build();

                        saveTargets.add(daily);

                    } else {    /* Add updated data - only when they are different */

                        Daily existingEntity = existingEntities.get(existingIndex);

                        Daily.DailyBuilder entityBuilder = Daily.builder();
                        boolean buildFlag = false;

                        if (!doubleHandler.compare(excelDailyDto.depositWithdrawalAmount(), existingEntity.getDepositWithdrawalAmount())) {
                            entityBuilder.depositWithdrawalAmount(excelDailyDto.depositWithdrawalAmount());
                            buildFlag = true;
                        }

                        if (!doubleHandler.compare(excelDailyDto.profitLossAmount(), existingEntity.getProfitLossAmount())) {
                            entityBuilder.profitLossAmount(excelDailyDto.profitLossAmount());
                            buildFlag = true;
                        }

                        if (buildFlag) {
                            saveTargets.add(entityBuilder
                                    .strategy(existingEntity.getStrategy())
                                    .id(existingEntity.getId())
                                    .date(existingEntity.getDate())

                                    .principal(0.0)
                                    .currentBalance(0.0)
                                    .standardAmount(0.0)
                                    .profitLossRate(0.0)
                                    .accumulatedProfitLossAmount(0.0)
                                    .accumulatedProfitLossRate(0.0)
                                    .build());
                        }
                    }

                }
                // Daily 저장
                dailyRepository.saveAll(saveTargets);
                // StrategyGraphAnalysis 저장
                saveTargets.forEach(daily -> strategyDetailService.saveAnalysis(strategyId, daily.getDate()));
                // Monthly 저장
                List<LocalDate> updatedDateList = saveTargets
                        .stream().map(Daily::getDate).collect(Collectors.toList());
                monthlyService.updateMonthly(strategyId, updatedDateList);

                // 계산 컬럼 저장하기
                if(!saveTargets.isEmpty()){
                    /* 계산 타겟을 추가하기 */
                    List<Daily> calculateTargets = dailyRepository.findByDateGreaterThanEqualAndStrategyOrderByDateAsc(saveTargets.get(0).getDate(), strategy);
                    List<Daily> calculatedTargets = new ArrayList<>(calculateTargets.size());

                    /* 계산하기 */
                    // 첫번째 요소
                    Daily firstCalculateTarget = calculateTargets.get(0);
                    Optional<Daily> optionalBeforeDaily = dailyRepository.findTop1ByDateBeforeAndStrategyOrderByDateDesc(firstCalculateTarget.getDate(),strategy);

                    boolean isFirst = optionalBeforeDaily.isEmpty();
                    Daily firstCalculateStandard = isFirst? null : optionalBeforeDaily.get();

                    calculatedTargets.add(
                            calculate(firstCalculateTarget, isFirst, firstCalculateStandard)
                    );

                    // 두번째 이후 요소
                    for (int i = 1; i < calculateTargets.size(); i++) {

                        Daily calculateTarget = calculateTargets.get(i);
                        Daily calculateStandard = calculateTargets.get(i-1);

                        calculatedTargets.add(
                                calculate(calculateTarget, false, calculateStandard)
                        );
                    }
                    dailyRepository.saveAll(calculatedTargets);
                }

            }

        } catch (IndexOutOfBoundsException e) {
            throw new InvalidFileFormatException("엑셀 업로드 실패");
        } catch (IOException e) {
            throw new InvalidFileFormatException("엑셀 업로드 실패: 파일 읽기에 실패했습니다.");
        } catch (EmptyFileException e) {
            throw new InvalidFileFormatException("엑셀 업로드 실패: 파일 내용이 올바르지 않습니다.");
        }

    }


    /**
     * 전략의 일간 데이터를 엑셀 파일로 리턴
     * @param strategyId 일간 데이터는 전략id 해당 전략에 대한 데이터
     * @return 엑셀 파일
     */
    @Override
    public InputStream downloadDailyExcel(Long strategyId) {

        List<Daily> entities = dailyRepository.findAllByStrategyIdOrderByDateAsc(strategyId);
        if(entities.isEmpty())
            throw new RuntimeException("다운로드 할 데이터가 없습니다");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily Transaction Data");

            // 날짜 형식
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("날짜");
            headerRow.createCell(1).setCellValue("입출금");
            headerRow.createCell(2).setCellValue("일 손익");

            int rowNum = 1;
            for (Daily daily : entities) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(daily.getDate());
                row.getCell(0).setCellStyle(dateCellStyle); // 날짜 형식 지정
                row.createCell(1).setCellValue(daily.getDepositWithdrawalAmount());
                row.createCell(2).setCellValue(daily.getProfitLossAmount());
            }

            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                workbook.write(byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                return new ByteArrayInputStream(byteArray);

            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 전략의 일간 데이터를 일간 통계와 함께 엑셀 파일로 리턴
     * @param strategyId 일간 데이터는 전략id 해당 전략에 대한 데이터
     * @return 엑셀 파일
     */
    @Override
    public InputStream downloadDailyExcelWithStatistics(Long strategyId) {

        List<Daily> entities = dailyRepository.findAllByStrategyIdOrderByDateAsc(strategyId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily Transaction Data");

            // 날짜 형식
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("날짜");
            headerRow.createCell(1).setCellValue("원금");
            headerRow.createCell(2).setCellValue("입출금");
            headerRow.createCell(3).setCellValue("일 손익");
            headerRow.createCell(4).setCellValue("일 손익률");
            headerRow.createCell(5).setCellValue("누적 손익");
            headerRow.createCell(6).setCellValue("누적 손익률");

            int rowNum = 1;
            for (Daily daily : entities) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(daily.getDate());
                row.getCell(0).setCellStyle(dateCellStyle); // 날짜 형식 지정
                row.createCell(1).setCellValue(daily.getPrincipal());
                row.createCell(2).setCellValue(daily.getDepositWithdrawalAmount());
                row.createCell(3).setCellValue(daily.getProfitLossAmount());
                row.createCell(4).setCellValue(daily.getProfitLossRate());
                row.createCell(5).setCellValue(daily.getAccumulatedProfitLossAmount());
                row.createCell(6).setCellValue(daily.getAccumulatedProfitLossRate());
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return new ByteArrayInputStream(byteArray);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public InputStream downloadMonthlyExcel(Long strategyId) {

        List<Monthly> entities = monthlyRepository.findAllByStrategyIdOrderByYearNumberAscMonthNumberAsc(strategyId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily Transaction Data");

            // 날짜 형식
            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle yearCellStyle = workbook.createCellStyle();
            yearCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy"));
            CellStyle monthCellStyle = workbook.createCellStyle();
            monthCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("mm"));

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("년도");
            headerRow.createCell(1).setCellValue("월");
            headerRow.createCell(2).setCellValue("월 평균 원금");
            headerRow.createCell(3).setCellValue("월 손익");
            headerRow.createCell(4).setCellValue("월 손익률");
            headerRow.createCell(5).setCellValue("누적 손익");
            headerRow.createCell(6).setCellValue("누적 손익률");

            int rowNum = 1;
            for (Monthly monthly : entities) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(monthly.getYearNumber());
                row.getCell(0).setCellStyle(yearCellStyle);
                row.createCell(1).setCellValue(monthly.getMonthNumber());
                row.getCell(1).setCellStyle(monthCellStyle);
                row.createCell(2).setCellValue(monthly.getAverageMonthlyPrincipal());
                row.createCell(3).setCellValue(monthly.getProfitLossAmount());
                row.createCell(4).setCellValue(monthly.getProfitLossRate());
                row.createCell(5).setCellValue(monthly.getAccumulatedProfitLossAmount());
                row.createCell(6).setCellValue(monthly.getAccumulatedProfitLossRate());
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return new ByteArrayInputStream(byteArray);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Excel 2007 이상 파일이 존재하는지 확인 (.xlsx)
     * @param file 검증할 파일
     * @return 사용 가능한 파일일 시 true
     */
    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.contains("spreadsheetml.sheet");
    }


    /**
     * 통계 컬럼을 계산하는 메서드
     * @param calculateTarget 계산할 Daily
     * @param isFirst 가장 날짜가 작은 엔티티인지
     * @param calculateStadard 계산 기준 (날짜 기준 직전 엔티티)
     * @return 계산된 Daily
     */
    private Daily calculate(Daily calculateTarget, boolean isFirst, Daily calculateStadard){

        Double deposit = calculateTarget.getDepositWithdrawalAmount();
        Double profit = calculateTarget.getProfitLossAmount();

        Double formerBalance = isFirst? null : calculateStadard.getCurrentBalance();
        Double formerStandard = isFirst? null : calculateStadard.getStandardAmount();
        Double formerPrincipal = isFirst? null : calculateStadard.getPrincipal();
        Double formerProfitAmount = isFirst? null : calculateStadard.getProfitLossAmount();
        Double formerProfitRate = isFirst? null : calculateStadard.getProfitLossRate();

        calculateTarget.setPrincipal(
                strategyCalculator.getPrincipal(
                        isFirst,
                        deposit,
                        formerPrincipal,
                        formerBalance
                ));
        calculateTarget.setCurrentBalance(
                strategyCalculator.getCurrentBalance(
                        isFirst,
                        formerBalance,
                        deposit,
                        profit
                )
        );
        calculateTarget.setStandardAmount(
                strategyCalculator.getStandardAmount(
                        isFirst,
                        deposit,
                        profit,
                        formerBalance,
                        formerPrincipal));
        calculateTarget.setProfitLossRate(
                strategyCalculator.getDailyProfitLossRate(
                        isFirst,
                        deposit,
                        profit,
                        formerBalance,
                        formerPrincipal,
                        formerStandard
                ));
        calculateTarget.setAccumulatedProfitLossAmount(
                strategyCalculator.getAccumulatedProfitLossAmount(
                        profit,
                        formerProfitAmount
                )
        );
        calculateTarget.setAccumulatedProfitLossRate(
                strategyCalculator.getAccumulatedProfitLossRate(
                        calculateTarget.getProfitLossRate(),
                        formerProfitRate
                )
        );

        return calculateTarget;
    }
}
