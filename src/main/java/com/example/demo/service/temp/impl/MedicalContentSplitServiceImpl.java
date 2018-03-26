package com.example.demo.service.temp.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.dao.temp.standard.ICHYXDao;
import com.example.demo.dao.temp.standard.PandianDao;
import com.example.demo.service.standard.IMedicalContentSplitService;
import com.example.demo.common.util.PatternUtil;
import com.example.demo.common.util.StringUtil;
import com.example.demo.common.util.TimeUtil;
import com.example.demo.entity.MedicalContentSplitModel;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

@Service
public class MedicalContentSplitServiceImpl implements IMedicalContentSplitService{

    @Autowired
    private ICHYXDao ichyxDao;

    @Autowired
    private PandianDao pandianDao;


    /**
     * 病历内容里面拆分点有日期格式的转换为日期+" :00"格式
     * @param medicalContent
     * @return
     */
    private String processMedicalContent(String medicalContent){
        Matcher dateMatcher = PatternUtil.DATE_PATTERN.matcher(medicalContent);
        int lastIndex = 0;
        StringBuilder sb = new StringBuilder();
        while(dateMatcher.find()){
            String temp = dateMatcher.group();
            int current = medicalContent.indexOf(temp, lastIndex);
            if(lastIndex != 0){
                sb.append(medicalContent.substring(lastIndex - 1 + 10, current));
            }
            if(current + 11 < medicalContent.length() && medicalContent.charAt(current + 11) == ' '){
                sb.append(temp + " 00:00");
            }else{
                sb.append(temp);
            }
            lastIndex = current + 1;
        }
        if(lastIndex != 0){
            sb.append(medicalContent.substring(lastIndex - 1 + 10));
            return sb.toString();
        }else{
            return medicalContent;
        }
    }

    @Override
    public boolean medicalContentSplit(String type){
        try {
            ichyxDao.changeJdbcTemplate(type);
            int medicalNameNotMatchCount = 0;
            int notSplitCount = 0;
            int notMatchCount = 0;
            int forbidCount = 0;
            int sum = 0;



            //更新记录的病历内容保存类
            List<MedicalContentSplitModel> updateResult = new ArrayList<>();

            //保存需要禁用的记录
            List<MedicalContentSplitModel> forbidList = new ArrayList<>();
            //额外添加的记录
            List<Map<String, Object>> addList = new ArrayList<>();

            File file = new File("/Users/liulun/Desktop/liulun.txt");
            FileWriter fileWriter = new FileWriter(file);

            List<Map<String, Object>> medicalContentCountMap = ichyxDao.findMedicalContentCountMap();
            fileWriter.write("分组条数:" + medicalContentCountMap.size() + "\n");
            System.out.println("分组条数:" + medicalContentCountMap.size());
            int length = CommonConstant.MEDICAL_CONTENT_TXT_SPLIT_PATTERN_LENGTH;
            if(CommonConstant.YXZW.equals(type) || CommonConstant.JKCT.equals(type)){
                length = CommonConstant.MEDICAL_CONTENT_SPLIT_PATTERN_LENGTH;
            }
            for(Map<String, Object> entity : medicalContentCountMap){
                //保存拆分的内容对应的时间
                Map<String, String> contentTimeMap = new HashMap<>();
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(entity));
                String medicalContent = jsonObject.getString(CommonConstant.MEDICAL_CONTENT);
                String visitNumber = jsonObject.getString(CommonConstant.A_VISIT_NUMBER);
                boolean isTxt = false;
                if("见txt文件".equals(medicalContent)){
                    medicalContent = readTxtContent(visitNumber, type);
                    isTxt = true;
                }
                Integer num = jsonObject.getIntValue("num");
                //Matcher matcher = PatternUtil.MEDICAL_CONTENT_SPLIT_PATTERN.matcher(medicalContent);
                Matcher matcher = PatternUtil.MEDICAL_CONTENT_TXT_SPLIT_PATTERN.matcher(medicalContent);
                if(CommonConstant.YXZW.equals(type) || CommonConstant.JKCT.equals(type)){
                    matcher = PatternUtil.MEDICAL_CONTENT_SPLIT_PATTERN.matcher(medicalContent);
                }
                int matchCount = 0;
                Map<Long, String> timeContentMap = new TreeMap<>();
                String lastTime = null;
                int lastIndex = 0;
                while(matcher.find()){
                    String time = matcher.group();
                    time = time.substring(0, time.length() - 1);
                    if(lastTime != null){

                        String content = medicalContent.substring(medicalContent.indexOf(lastTime,lastIndex) + length, medicalContent.indexOf(time, medicalContent.indexOf(lastTime,lastIndex) + 1));
                        lastIndex = medicalContent.indexOf(lastTime, lastIndex) + 1;
                        long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
                        while(timeContentMap.containsKey(key)){
                            key++;
                        }
                        timeContentMap.put(key, content);
                        contentTimeMap.put(StringUtil.trim(content), lastTime);
                    }
                    lastTime = time;
                    matchCount++;
                }
                if(lastTime != null) {
                    long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
                    while(timeContentMap.containsKey(key)){
                        key++;
                    }
                    String content = medicalContent.substring(medicalContent.indexOf(lastTime, lastIndex) + length);
                    timeContentMap.put(key, content);
                    contentTimeMap.put(StringUtil.trim(content), lastTime);
                }
                //如果匹配条数小于数据库记录条数，有可能存在不是日期+时间分割的拆分点，只有一个日期存在
                if(matchCount != 0 && matchCount < num){
                    String formatMedicalContent = processMedicalContent(medicalContent);
                    matcher = PatternUtil.MEDICAL_CONTENT_TXT_SPLIT_PATTERN.matcher(formatMedicalContent);
                    if(CommonConstant.YXZW.equals(type) || CommonConstant.JKCT.equals(type)){
                        matcher = PatternUtil.MEDICAL_CONTENT_SPLIT_PATTERN.matcher(medicalContent);
                    }
                    matchCount = 0;
                    timeContentMap.clear();
                    contentTimeMap.clear();
                    lastTime = null;
                    lastIndex = 0;
                    while(matcher.find()){
                        String time = matcher.group();
                        time = time.substring(0, time.length() - 1);
                        if(lastTime != null){
                            String content = formatMedicalContent.substring(formatMedicalContent.indexOf(lastTime,lastIndex) + length,
                                    formatMedicalContent.indexOf(time, formatMedicalContent.indexOf(lastTime,lastIndex) + 1));
                            lastIndex = formatMedicalContent.indexOf(lastTime, lastIndex) + 1;
                            long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
                            while(timeContentMap.containsKey(key)){
                                key++;
                            }
                            timeContentMap.put(key, content);
                            contentTimeMap.put(StringUtil.trim(content), lastTime);
                        }
                        lastTime = time;
                        matchCount++;
                    }
                    if(lastTime != null) {
                        long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
                        while(timeContentMap.containsKey(key)){
                            key++;
                        }
                        String content = formatMedicalContent.substring(formatMedicalContent.indexOf(lastTime, lastIndex) + length);
                        timeContentMap.put(key, content);
                        contentTimeMap.put(StringUtil.trim(content), lastTime);
                    }
                }
                if(matchCount == 0){
                    MedicalContentSplitModel medicalContentSplitModel = new MedicalContentSplitModel();
                    medicalContentSplitModel.setVisitNumber(jsonObject.getString(CommonConstant.A_VISIT_NUMBER));
                    medicalContentSplitModel.setMedicalContent(jsonObject.getString(CommonConstant.MEDICAL_CONTENT));
                    forbidList.add(medicalContentSplitModel);
                    //fileWriter.write("未匹配记录:" + jsonObject + "\n");
                    notMatchCount++;
                    forbidCount += num;
                    continue;
                }
                if(num != matchCount){
                    MedicalContentSplitModel medicalContentSplitModel = new MedicalContentSplitModel();
                    medicalContentSplitModel.setVisitNumber(jsonObject.getString(CommonConstant.A_VISIT_NUMBER));
                    medicalContentSplitModel.setMedicalContent(jsonObject.getString(CommonConstant.MEDICAL_CONTENT));
                    forbidList.add(medicalContentSplitModel);
                    fileWriter.write("数据库记录匹配条数：" + num + "\n");
                    fileWriter.write("实际匹配条数:" + matchCount + "\n");
                    fileWriter.write("medicalContent:" + medicalContent + "\n");
                    notSplitCount++;
                    //return false;
                }else{
                    sum += matchCount;
                    List<String> contentList = new ArrayList<>();
                    List<Integer> contentUseFlagList = new ArrayList<>();
                    for(Long key : timeContentMap.keySet()){
                        contentList.add(StringUtil.trim(timeContentMap.get(key)));
                        contentUseFlagList.add(0);
                    }
                    List<Map<String, Object>> createDateMedicalNameMap = ichyxDao.findCreateDateMedicalNameMapByVisitNumberAndMedicalContent(visitNumber, isTxt ? "见txt文件" : medicalContent);
                    if(createDateMedicalNameMap.size() != matchCount){
                        MedicalContentSplitModel medicalContentSplitModel = new MedicalContentSplitModel();
                        medicalContentSplitModel.setVisitNumber(jsonObject.getString(CommonConstant.A_VISIT_NUMBER));
                        medicalContentSplitModel.setMedicalContent(jsonObject.getString(CommonConstant.MEDICAL_CONTENT));
                        forbidList.add(medicalContentSplitModel);
                        fileWriter.write("数据库查询记录条数:" + createDateMedicalNameMap.size() + "\n");
                        fileWriter.write("分拆匹配条数:" + matchCount + "\n");
                        fileWriter.write("一次就诊号:" + visitNumber + "\n");
                        fileWriter.write("medicalContent: " + medicalContent + "\n");
                        forbidCount += createDateMedicalNameMap.size();
                        continue;
                    }
                    Collections.sort(createDateMedicalNameMap, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                        Long firstCreateDate = TimeUtil.dateStringToLong((String)o1.get(CommonConstant.CREATE_DATE));
//                        Long secondCreateDate = TimeUtil.dateStringToLong((String)o2.get(CommonConstant.CREATE_DATE));
                            Long firstCreateDate = TimeUtil.txtDateStringToLong((String)o1.get(CommonConstant.CREATE_DATE));
                            Long secondCreateDate = TimeUtil.txtDateStringToLong((String)o2.get(CommonConstant.CREATE_DATE));
                            if(firstCreateDate.longValue() == secondCreateDate){
                                return 0;
                            }else if(firstCreateDate.longValue() > secondCreateDate){
                                return 1;
                            }else{
                                return -1;
                            }
                            //return (int)(firstCreateDate - secondCreateDate);
                        }
                    });
                    int i = 0;
                    List<MedicalContentSplitModel> notUpdateContent = new ArrayList<>();
                    for(Map<String, Object> createDateMedicalName : createDateMedicalNameMap){
                        String createDate = (String)createDateMedicalName.get(CommonConstant.CREATE_DATE);
                        String medicalName = (String)createDateMedicalName.get(CommonConstant.MEDICAL_NAME);
                        MedicalContentSplitModel medicalContentSplitModel = new MedicalContentSplitModel();
                        medicalContentSplitModel.setCreateDate(createDate);
                        medicalContentSplitModel.setVisitNumber(visitNumber);
                        if(matchCount != num){
                            medicalContentSplitModel.setStatus(2);
                        }else{
                            medicalContentSplitModel.setStatus(0);
                        }
                        String content = "";
                        if(i < contentList.size()){
                            content = contentList.get(i);
                        }
                        if(medicalName.contains("(")){
                            medicalName = medicalName.substring(0, medicalName.indexOf("("));
                        }
                        if(medicalName.contains("（")){
                            medicalName = medicalName.substring(0, medicalName.indexOf("（"));
                        }
                        if(medicalName.endsWith("X2")){
                            medicalName = medicalName.substring(0, medicalName.lastIndexOf("X2"));
                        }
                        if(content.startsWith(medicalName)){
                            medicalContentSplitModel.setMedicalContent(content);
                            contentUseFlagList.set(i, 1);
                            updateResult.add(medicalContentSplitModel);
                        }else{
                            boolean matchFlag = false;
                            for(int m = 0; m < contentList.size(); m++){
                                if(contentUseFlagList.get(m) == 0 && contentList.get(m).startsWith(medicalName)){
                                    //同时按照时间查询出来的数据库病历名称第m位不能是第m位内容的开头文本
                                    if(m >= createDateMedicalNameMap.size() || !contentList.get(m).startsWith((String)createDateMedicalNameMap.get(m).get(CommonConstant.MEDICAL_NAME))) {
                                        medicalContentSplitModel.setMedicalContent(contentList.get(m));
                                        updateResult.add(medicalContentSplitModel);
                                        contentUseFlagList.set(m, 1);
                                        matchFlag = true;
                                        break;
                                    }
                                }
                            }
                            if(!matchFlag){
                            /*fileWriter.write("全部内容：" + medicalContent + "\n");
                            fileWriter.write("子内容:" + content + "\n");
                            fileWriter.write("病历名称:" + medicalName + "\n");
                            fileWriter.write("一次就诊号：" + visitNumber + "\n");*/
                                if(!"".equals(content)){
                                    notUpdateContent.add(medicalContentSplitModel);
                                    medicalNameNotMatchCount++;
                                }else{
                                    forbidList.add(medicalContentSplitModel);
                                }
                            }

                            //return false;
                        }
                        i++;
                    }
                    int notUpdateContentSize = notUpdateContent.size();
                    if(matchCount == num){
                        //还未匹配的记录行内容根据时间先后顺序匹配分割的内容
                        for(int m = 0; m < notUpdateContent.size(); m++){
                            for(int n = 0; n < contentUseFlagList.size(); n++){
                                if(contentUseFlagList.get(n) == 0){
                                    contentUseFlagList.set(n, 1);
                                    notUpdateContent.get(m).setMedicalContent(contentList.get(n));
                                    updateResult.add(notUpdateContent.get(m));
                                    notUpdateContentSize--;
                                    break;
                                }
                            }
                        }
                    }

                    if(notUpdateContentSize != 0){
                        for(MedicalContentSplitModel medicalContentSplitModel : notUpdateContent) {
                            //fileWriter.write("还有未更新病历内容的行" + medicalContentSplitModel + "\n");
                            forbidList.add(medicalContentSplitModel);
                        }
                    }

                    Map<String, Object> singleMap = null;
                    for(int m = 0; m < contentUseFlagList.size(); m++){
                        if(contentUseFlagList.get(m) == 0){
                            if(singleMap == null) {
                                singleMap = ichyxDao.findByVisitNumberAndMedicalContentLimitOne(visitNumber, isTxt ? "见txt文件" : medicalContent);
                            }
                            String content = contentList.get(m);
                            Map<String, Object> addEntity = new HashMap<>();
                            addEntity.putAll(singleMap);
                            addEntity.remove("id");
                            addEntity.put(CommonConstant.UPDATE_CONTENT, content);
                            //胰腺站位和健康查体需要格式化时间
                            //addEntity.put(CommonConstant.CREATE_DATE, TimeUtil.medicalContentTimeFormat(contentTimeMap.get(content)));
                            addEntity.put(CommonConstant.CREATE_DATE, contentTimeMap.get(content));
                            addEntity.put(CommonConstant.RECORD_DATE, addEntity.get(CommonConstant.CREATE_DATE));
                            addEntity.put(CommonConstant.STATUS, 3);
                            int recordIndex = content.indexOf("记录");
                            if(recordIndex != -1){
                                addEntity.put(CommonConstant.MEDICAL_NAME, content.substring(0, recordIndex + 2));
                            }else{
                                addEntity.put(CommonConstant.MEDICAL_NAME, "未匹配病历名称");
                            }
                            addList.add(addEntity);
                        }
                    }
                }
            }
//            for(MedicalContentSplitModel medicalContentSplitModel : forbidList){
//                fileWriter.write("禁用行:" + medicalContentSplitModel + "\n");
//            }
//            for(Map<String, Object> add : addList){
//                fileWriter.write("添加行:" + add + "\n");
//            }
//            for(MedicalContentSplitModel medicalContentSplitModel : updateResult){
//                fileWriter.write(medicalContentSplitModel.getCreateDate() + "$#$" + medicalContentSplitModel.getVisitNumber() + "$#$" + medicalContentSplitModel.getMedicalContent() + "\n");
//            }
            int result = forbidDatabase(forbidList);
            fileWriter.write("禁用:" + result + "\n");
            result = updateDatabase(updateResult);
            fileWriter.write("更新:" + result + "\n");
            addDatabase(addList);
            fileWriter.close();
            System.out.println("medicalNameNotMatchCount:" + medicalNameNotMatchCount);
            System.out.println("notMatchCount:" + notMatchCount);
            System.out.println("notSplitCount:" + notSplitCount);
            System.out.println("forbidCount:" + forbidCount);
            System.out.println(updateResult.size() + " " + sum);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Set<String> datacul(String sql) {
        try{
            int sum = 0;
            ichyxDao.changeJdbcTemplate(CommonConstant.JKCT);
            List<String> jkct = ichyxDao.datacul(sql);
            sum += jkct.size();
            ichyxDao.changeJdbcTemplate(CommonConstant.YX);
            List<String> yx = ichyxDao.datacul(sql);
            sum += yx.size();
            ichyxDao.changeJdbcTemplate(CommonConstant.YXZW);
            List<String> yxzw = ichyxDao.datacul(sql);
            sum += yxzw.size();
            ichyxDao.changeJdbcTemplate(CommonConstant.TNB);
            List<String> tnb = ichyxDao.datacul(sql);
            sum += tnb.size();
            System.out.println(tnb.size() + " " + sum);
            Set<String> result = new HashSet<>();
            for(String value : jkct){
                result.add(value);
            }
            for(String value : yx){
                result.add(value);
            }
            for(String value : yxzw){
                result.add(value);
            }
            for(String value : tnb){
                result.add(value);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    @Override
    public Integer dataculAdd(String sql) {
        try{
            int sum = 0;
            ichyxDao.changeJdbcTemplate(CommonConstant.JKCT);
            Integer jkct = ichyxDao.dataculAdd(sql);
            sum += jkct;
            ichyxDao.changeJdbcTemplate(CommonConstant.YX);
            Integer yx = ichyxDao.dataculAdd(sql);
            sum += yx;
            ichyxDao.changeJdbcTemplate(CommonConstant.YXZW);
            Integer yxzw = ichyxDao.dataculAdd(sql);
            sum += yxzw;
            ichyxDao.changeJdbcTemplate(CommonConstant.TNB);
            Integer tnb = ichyxDao.dataculAdd(sql);
            sum += tnb;
            return sum;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private void groupCount(Map<String, Integer[]> result, String sql, String mapping) {
        try{
            ichyxDao.changeJdbcTemplate(CommonConstant.JKCT);
            List<Map<String, Object>> jkct = ichyxDao.groupCount(sql);
            ichyxDao.changeJdbcTemplate(CommonConstant.YX);
            List<Map<String, Object>> yx = ichyxDao.groupCount(sql);
            ichyxDao.changeJdbcTemplate(CommonConstant.YXZW);
            List<Map<String, Object>> yxzw = ichyxDao.groupCount(sql);
            ichyxDao.changeJdbcTemplate(CommonConstant.TNB);
            List<Map<String, Object>> tnb = ichyxDao.groupCount(sql);
            jkct.addAll(yx);
            jkct.addAll(yxzw);
            jkct.addAll(tnb);
            for(Map<String, Object> value : jkct){
                String id = (String)value.get("病人ID号");
                if(value.containsKey("mapping")){
                    mapping = (String)value.get("mapping");
                }
                Integer count = ((Long)value.get("num")).intValue();
                if(!result.containsKey(id)) {
                    Integer[] arr = new Integer[10];
                    for(int i = 0; i < 10; i++){
                        arr[i] = 0;
                    }
                    result.put(id, arr);
                }
                if(mapping.startsWith("入院记录")){
                    result.get(id)[0] += count;
                }else if(mapping.startsWith("出院记录")){
                    result.get(id)[1] += count;
                }else if(mapping.startsWith("化验记录")){
                    result.get(id)[2] += count;
                }else if(mapping.startsWith("检查记录_表格")){
                    result.get(id)[3] += count;
                }else if(mapping.startsWith("检查记录")){
                    result.get(id)[4] += count;
                }else if(mapping.startsWith("手术操作记录")){
                    result.get(id)[5] += count;
                }else if(mapping.startsWith("治疗方案")){
                    result.get(id)[6] += count;
                }else if(mapping.startsWith("门诊记录")){
                    result.get(id)[7] += count;
                }else if(mapping.startsWith("其他记录")){
                    result.get(id)[8] += count;
                }else if(mapping.startsWith("病理")){
                    result.get(id)[9] += count;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean importYXXGExcel() {
        try{
            List<String> headList = new ArrayList<>();

            File   file =  new File("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/检验明细");
            File[] childFileArr = file.listFiles();
            for(File childFile : childFileArr){
                List<Map<String, Object>> result = excelParse(null, headList, childFile.getName());
                String filePath = "/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/检验明细" +
                        childFile.getName().substring(0, childFile.getName().lastIndexOf(".")) + ".txt";
                File loadData = new File(filePath);
                if(!loadData.exists()){
                    loadData.createNewFile();
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(loadData));
                for(int i = 0; i < result.size(); i++){
                    StringBuilder sb = new StringBuilder();
                    Map<String, Object> data = result.get(i);
                    for(int m = 0; m < headList.size(); m++){
                        sb.append(data.get(headList.get(m)) == null ? "\\N" : data.get(headList.get(m)));
                        sb.append("#$#");
                    }
                    bufferedWriter.write(sb.substring(0, sb.length() - 3) + "\n");
                }
                bufferedWriter.flush();
                bufferedWriter.close();
                ichyxDao.changeJdbcTemplate(CommonConstant.YX);
                ichyxDao.executeSql("load data local infile \"" + filePath + "\" into table `检验报告明细`\n" +
                        "fields TERMINATED by '#$#' \n" +
                        "LINES TERMINATED by '\n'");
//                int count = result.size() / 1000;
//                if(result.size() % 1000 != 0){
//                    count += 1;
//                }
//                for(int m = 0; m < count; m++){
//                    System.out.println(m);
//                    ichyxDao.changeJdbcTemplate(CommonConstant.YX);
//                    ichyxDao.addCheckDetail(headList, result.subList(m*1000, (m + 1)*1000 > result.size() ? result.size() : (m+1)*1000));
//                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public JSONObject pandian() {
        JSONObject jsonObject = new JSONObject();
        String sql = "select distinct 病人ID号 from 患者基本信息";
        Set<String> userSet = datacul(sql);
        jsonObject.put("患者总数（PID）", userSet.size());
        sql = "select distinct 一次就诊号 from 病历文书 where mapping like '入院记录%' or mapping like '出院记录%'";
        Set<String> visitNumSet = datacul(sql);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '入院记录%'";
        int ruyuancount = dataculAdd(sql);
        jsonObject.put("入院记录", ruyuancount);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '出院记录%'";
        int chuyuancount = dataculAdd(sql);
        jsonObject.put("出院记录", chuyuancount);
        jsonObject.put("病历记录汇总", (ruyuancount + chuyuancount));
        sql = "select count(distinct 检验申请号) from 检验报告明细";
        int huayan = dataculAdd(sql);
        sql = "select count(distinct 检验申请号) from 微生物报告明细";
        huayan += dataculAdd(sql);
        jsonObject.put("化验记录", huayan);
        jsonObject.put("表格病历", huayan);
        sql = "select count(一次就诊号) from 检查报告";
        int jianchaTable = dataculAdd(sql);
        jsonObject.put("检查记录_表格", jianchaTable);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '检查记录%'";
        int jianchaText = dataculAdd(sql);
        jsonObject.put("检查记录_文本", jianchaText);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '手术操作记录%'";
        int shoushu = dataculAdd(sql);
        jsonObject.put("手术操作记录", shoushu);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '治疗方案%'";
        int zhiliao = dataculAdd(sql);
        jsonObject.put("治疗方案", zhiliao);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '门诊记录%'";
        int menzhen = dataculAdd(sql);
        jsonObject.put("门诊记录", menzhen);
        sql = "select count(一次就诊号) from 病历文书 where mapping like '其他记录%'";
        int other = dataculAdd(sql);
        jsonObject.put("其他记录", other);
        sql = "select count(*) from 病理";
        int bingli = dataculAdd(sql);
        jsonObject.put("病理", bingli);

        jsonObject.put("住院号总数", visitNumSet.size());
        sql = "select distinct 一次就诊号 from 病历文书 where mapping like '入院记录%'";
        Set<String> ruyuan = datacul(sql);
        sql = "select distinct 一次就诊号 from 病历文书 where mapping like '出院记录%'";
        Set<String> chuyuan = datacul(sql);
        ruyuan.retainAll(chuyuan);
        jsonObject.put("单次完整出入院盘点", ruyuan.size());
        sql = "select 病人ID号,mapping,count(*) num from 病历文书 where mapping is not null group by 病人ID号,mapping";
        Map<String, Integer[]> result = new HashMap<>();
        groupCount(result, sql, null);
        sql = "select b.病人ID号 病人ID号, count(distinct a.检验申请号) num from 检验报告明细 a left join `患者基本信息` b on a.`一次就诊号` = b.`一次就诊号` group by b.`病人ID号`";
        groupCount(result, sql, "化验记录");
        sql = "select b.病人ID号 病人ID号, count(distinct a.检验申请号) num from 微生物报告明细 a left join `患者基本信息` b on a.`一次就诊号` = b.`一次就诊号` group by b.`病人ID号`";
        groupCount(result, sql, "化验记录");
        sql = "select 病人ID号, count(*) num from 检查报告 group by 病人ID号";
        groupCount(result, sql, "检查记录_表格");
        sql = "select 病人ID号, count(*) num from 病理 group by 病人ID号";
        groupCount(result, sql, "病理");
        List<String> notFoungList = new ArrayList<>();
        for(String key : result.keySet()){
            if(!userSet.contains(key)){
                notFoungList.add(key);
            }
        }
        jsonObject.put("出入院未找到患者ID列表", notFoungList);
        for(String key : userSet){
            if(!result.containsKey(key)){
                result.put(key, initArr(10));
            }
        }
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾",
                "住院详单", "xlsx", result, CommonConstant.RECORD_TYPE);
        return jsonObject;
    }

    private void processJSONObjectList(List<JSONObject> jsonObjectList, Set<String> singleSet, Set<String> allSet,
                                       Map<String, String> anchorOriginalMap, Map<String ,Integer> anchorCountMap){
        for(JSONObject value : jsonObjectList){
            String groupRecordName =value.getString("groupRecordName");
            allSet.add(groupRecordName);
            singleSet.add(groupRecordName);
            String text = value.getJSONObject("info").getString("text");
            Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(text);
            while(matcher.find()){
                String anchor = matcher.group(1);
                if(!anchorCountMap.containsKey(anchor)){
                    anchorCountMap.put(anchor, 1);
                    anchorOriginalMap.put(anchor, text);
                }else{
                    anchorCountMap.put(anchor, anchorCountMap.get(anchor) + 1);
                }
            }
        }
    }

    @Override
    public JSONObject mongoPandian(String batchNo) {
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> userList = pandianDao.findIdByBatchNo(batchNo);
        jsonObject.put("患者总数（PID）", userList.size());
        Set<String> userSet = new HashSet<>();
        for(JSONObject user : userList){
            userSet.add(user.getString("_id"));
        }
        DBObject dbObject = new BasicDBObject();
        dbObject.put("batchNo", batchNo);
        dbObject.put("recordType", "入院记录");
        dbObject.put("deleted", false);
        DBObject fieldObject = new BasicDBObject();
        fieldObject.put("patientId", true);
        fieldObject.put("groupRecordName", true);
        fieldObject.put("info.text", true);
        Query ruyuanQuery = new BasicQuery(dbObject, fieldObject);
        List<JSONObject> ruyuan = pandianDao.findListByQuery(ruyuanQuery, "Record");
        jsonObject.put("入院记录", ruyuan.size());
        System.out.println(jsonObject);
        DBObject chuyuanObject = new BasicDBObject();
        chuyuanObject.put("batchNo", batchNo);
        chuyuanObject.put("recordType", "出院记录");
        chuyuanObject.put("deleted", false);
        Query chuyuanQuery = new BasicQuery(chuyuanObject, fieldObject);
        List<JSONObject> chuyuan = pandianDao.findListByQuery(chuyuanQuery, "Record");
        jsonObject.put("出院记录", chuyuan.size());
        jsonObject.put("病历记录汇总", pandianDao.findCountByQuery(new Query().addCriteria(Criteria.where("batchNo").is(batchNo))
                .addCriteria(Criteria.where("deleted").is(false)), "Record"));
        System.out.println(jsonObject);
        Set<String> groupRecordNameSet = new HashSet<>();
        Set<String> ruyuanSet =new HashSet<>();
        Set<String> chuyuanSet = new HashSet<>();
        Map<String, String> anchorOriginalMap = new HashMap<>();
        Map<String ,Integer> anchorCountMap = new HashMap<>();
        processJSONObjectList(ruyuan, ruyuanSet, groupRecordNameSet, anchorOriginalMap, anchorCountMap);
        processJSONObjectList(chuyuan, chuyuanSet, groupRecordNameSet, anchorOriginalMap, anchorCountMap);
        jsonObject.put("住院号总数", groupRecordNameSet.size());
        System.out.println(jsonObject);
        ruyuanSet.retainAll(chuyuanSet);
        jsonObject.put("单次完整出入院盘点", ruyuanSet.size());
        System.out.println(jsonObject);
        Set<String> notFoundSet =new HashSet<>();
        Map<String, Integer[]> result =new HashMap<>();
        for(JSONObject value : ruyuan){
            String id =value.getString("patientId");
            if(!userSet.contains(id)){
                notFoundSet.add(id);
            }
            if(!result.containsKey(id)){
                result.put(id, initArr(10));

            }
            result.get(id)[0] += 1;
        }
        for(JSONObject value : chuyuan){
            String id =value.getString("patientId");
            if(!userSet.contains(id)){
                notFoundSet.add(id);
            }
            if(!result.containsKey(id)){
                result.put(id, initArr(10));
            }
            result.get(id)[1] += 1;
        }
        for(int i = 3; i < CommonConstant.RECORD_TYPE.length; i++){
            System.out.println(CommonConstant.RECORD_TYPE[i]);
            DBObject loopFieldObject = new BasicDBObject();
            loopFieldObject.put("patientId", true);
            DBObject loopQueryObject = new BasicDBObject();
            loopQueryObject.put("batchNo", batchNo);
            if("检查记录_表格".equals(CommonConstant.RECORD_TYPE[i])){
                loopQueryObject.put("recordType", "检查记录");
                loopQueryObject.put("format", "table");
            }else if("检查记录_文本".equals(CommonConstant.RECORD_TYPE[i])){
                loopQueryObject.put("recordType", "检查记录");
                loopQueryObject.put("format", "text");
            }else{
                loopQueryObject.put("recordType", CommonConstant.RECORD_TYPE[i]);
            }
            loopQueryObject.put("deleted", false);
            List<JSONObject> loopResult = pandianDao.findListByQuery(new BasicQuery(loopQueryObject, loopFieldObject), "Record");
            jsonObject.put(CommonConstant.RECORD_TYPE[i], loopResult.size());
            for(JSONObject value : loopResult){
                String id =value.getString("patientId");
                if(!userSet.contains(id)){
                    notFoundSet.add(id);
                }
                if(!result.containsKey(id)){
                    result.put(id, initArr(10));

                }
                result.get(id)[i - 1] += 1;
            }
        }
        for(String key : userSet){
            if(!result.containsKey(key)){
                result.put(key, initArr(10));
            }
        }
        jsonObject.put("表格病历", pandianDao.findCountByQuery(new Query()
                .addCriteria(Criteria.where("batchNo").is(batchNo))
                .addCriteria(Criteria.where("format").is("text"))
                .addCriteria(Criteria.where("deleted").is(false)), "Record"));
        //jsonObject.put("表格病历", jsonObject.getInteger("化验记录"));
        jsonObject.put("出入院未找到患者ID列表", notFoundSet);
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "锚点原文对应表(mongo)", "xlsx", anchorOriginalMap, anchorCountMap, new String[]{"锚点", "数量", "原文"});
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "住院详单(mongo)", "xlsx", result, CommonConstant.RECORD_TYPE);
        return jsonObject;
    }

    private Integer[] initArr(int length){
        Integer[] arr = new Integer[length];
        for(int m = 0; m < length; m++){
            arr[m] = 0;
        }
        return arr;
    }



    private void writer(String path, String fileName,String fileType,Map<String, Integer[]> result,String titleRow[]) {
        try{
            Workbook wb = null;
            String excelPath = path+File.separator+fileName+"."+fileType;
            File file = new File(excelPath);
            Sheet sheet =null;
            //创建工作文档对象
            if (!file.exists()) {
                if (fileType.equals("xls")) {
                    wb = new HSSFWorkbook();

                } else if(fileType.equals("xlsx")) {

                    wb = new XSSFWorkbook();
                } else {
                    throw new RuntimeException("文件格式不正确");
                }
                //创建sheet对象
                sheet = (Sheet) wb.createSheet("住院详单");
                OutputStream outputStream = new FileOutputStream(excelPath);
                wb.write(outputStream);
                outputStream.flush();
                outputStream.close();

            } else {
                if (fileType.equals("xls")) {
                    wb = new HSSFWorkbook();

                } else if(fileType.equals("xlsx")) {
                    wb = new XSSFWorkbook();

                } else {
                    throw new RuntimeException("文件格式不正确");
                }
            }
            //创建sheet对象
            if (sheet==null) {
                sheet = (Sheet) wb.createSheet("住院详单");
            }

            //添加表头
            Row row = sheet.createRow(0);
            Cell cell;
            for(int i = 0;i < titleRow.length;i++){
                cell = row.createCell(i);
                cell.setCellValue(titleRow[i]);
            }
            int rowIndex = 0;
            for(String key : result.keySet()){
                row = sheet.createRow(++rowIndex);
                cell = row.createCell(0);
                if(key.startsWith("shch_")){
                    cell.setCellValue(key);
                }else {
                    cell.setCellValue("shch_" + key);
                }
                Integer[] arr = result.get(key);
                for(int i = 0; i < arr.length; i++){
                    cell = row.createCell(i + 1);
                    cell.setCellValue(arr[i]);
                }
            }

            //创建文件流
            OutputStream stream = new FileOutputStream(excelPath);
            //写入数据
            wb.write(stream);
            //关闭文件流
            stream.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void writer(String path, String fileName,String fileType,Map<String, String> anchorOriginalMap,
                              Map<String, Integer> anchorCountMap,String titleRow[]){
        try{

            Workbook wb = null;
            String excelPath = path+File.separator+fileName+"."+fileType;
            File file = new File(excelPath);
            Sheet sheet =null;
            //创建工作文档对象
            if (!file.exists()) {
                if (fileType.equals("xls")) {
                    wb = new HSSFWorkbook();

                } else if(fileType.equals("xlsx")) {

                    wb = new XSSFWorkbook();
                } else {
                    throw new RuntimeException("文件格式不正确");
                }
                //创建sheet对象
                sheet = (Sheet) wb.createSheet("锚点原文对应表");
                OutputStream outputStream = new FileOutputStream(excelPath);
                wb.write(outputStream);
                outputStream.flush();
                outputStream.close();

            } else {
                if (fileType.equals("xls")) {
                    wb = new HSSFWorkbook();

                } else if(fileType.equals("xlsx")) {
                    wb = new XSSFWorkbook();

                } else {
                    throw new RuntimeException("文件格式不正确");
                }
            }
            //创建sheet对象
            if (sheet==null) {
                sheet = (Sheet) wb.createSheet("锚点原文对应表");
            }

            //添加表头
            Row row = sheet.createRow(0);
            Cell cell;
            for(int i = 0;i < titleRow.length;i++){
                cell = row.createCell(i);
                cell.setCellValue(titleRow[i]);
            }
            Iterator<String> keys = anchorOriginalMap.keySet().iterator();
            int rowIndex = 0;
            while(keys.hasNext()){
                String key = keys.next();
                String value = anchorOriginalMap.get(key);
                row = sheet.createRow(++rowIndex);
                cell = row.createCell(0);
                cell.setCellValue(key);
                cell = row.createCell(1);
                cell.setCellValue(anchorCountMap.get(key));
                cell = row.createCell(2);
                cell.setCellValue(value.replaceAll("【【", "\n【【"));
            }

            //创建文件流
            OutputStream stream = new FileOutputStream(excelPath);
            //写入数据
            wb.write(stream);
            //关闭文件流
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private int updateDatabase(List<MedicalContentSplitModel> update){
        int result = 0;
        for(int i = 0; i < update.size(); i++){
            int temp = ichyxDao.update(update.get(i));
            if(temp == 0){
                System.out.println(update.get(i));
            }
            result += temp;
        }
        return result;
    }

    private int forbidDatabase(List<MedicalContentSplitModel> forbid){
        int result = 0;
        for(int i = 0; i < forbid.size(); i++){
            result += ichyxDao.forbid(forbid.get(i));
        }
        return result;
    }

    private void addDatabase(List<Map<String, Object>> add){
        for(int i  = 0; i < add.size(); i++){
            ichyxDao.add(add.get(i));
        }
    }

    private static String getStringCellValue(Cell cell) {
        String strCell = "";
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_STRING:
                strCell = cell.getStringCellValue();
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                strCell = String.valueOf(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                strCell = "";
                break;
            default:
                strCell = "";
                break;
        }
        if (strCell.equals("") || strCell == null) {
            return null;
        }
        return strCell;
    }

    public static List<Map<String, Object>> excelParse(String filePath, List<String> headList, String fileName) throws Exception{
        System.out.println(fileName);
        if(StringUtils.isEmpty(filePath)){
            filePath = "/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/检验明细";
        }
        File file = new File(filePath + "/" + fileName);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
        List<Map<String, Object>> data = new ArrayList<>();
        int sheetNum = xssfWorkbook.getNumberOfSheets();
        for(int k = 0; k < sheetNum; k++){
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(k);
            int rowEnd = xssfSheet.getLastRowNum();
            System.out.println(rowEnd);
            XSSFRow headRow = xssfSheet.getRow(0);
            int column = headRow.getPhysicalNumberOfCells();
            if(headList.size() == 0){
                for(int i = 0; i < column; i++){
                    headList.add(getStringCellValue(headRow.getCell(i)));
                }
            }
            for(int i = 1; i <= rowEnd; i++){
                XSSFRow row = xssfSheet.getRow(i);
                if(row == null) {
                    continue;
                }
                Map<String, Object> result = new HashMap<>();
                for(int j = 0; j < column; j++){
                    result.put(headList.get(j), getStringCellValue(row.getCell(j)));
                }
                data.add(result);
            }
        }
//        for(int i = 0; i < data.size(); i++){
//            System.out.println(data.get(i));
//        }
        return data;
    }

    private String readTxtContent(String filePrefix, String type) throws Exception{
        File file;
        if(CommonConstant.TNB.equals(type)){
            file = new File(("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/胰腺相关病历"));
        }else if(CommonConstant.YX.equals(type)){
            file = new File(("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/糖尿病病历"));
        }else{
            throw new Exception("类型错误");
        }
        File[] childFileArr = file.listFiles();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < childFileArr.length; i++){
            if(childFileArr[i].getName().startsWith(filePrefix)){
                InputStreamReader isr = new InputStreamReader(new FileInputStream(childFileArr[i]), "GBK");
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
            }

        }
        return sb.toString();
    }



    public static void main(String[] args) throws Exception{
        /*try{
            List<String> headList = new ArrayList<>();
            File loadData = new File("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/loadData.txt");
            if(!loadData.exists()){
                loadData.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(loadData));
            File   file =  new File("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/检验明细");
            File[] childFileArr = file.listFiles();
            for(File childFile : childFileArr){
                List<Map<String, Object>> result = excelParse(null, headList, childFile.getName());
                for(int i = 0; i < result.size(); i++){
                    StringBuilder sb = new StringBuilder();
                    Map<String, Object> data = result.get(i);
                    for(int m = 0; m < headList.size(); m++){
                        sb.append(data.get(headList.get(m)) == null ? "\\N" : data.get(headList.get(m)));
                        sb.append("#$#");
                    }
                    bufferedWriter.write(sb.substring(0, sb.length() - 3) + "\n");
                }
//                int count = result.size() / 1000;
//                if(result.size() % 1000 != 0){
//                    count += 1;
//                }
//                for(int m = 0; m < count; m++){
//                    System.out.println(m);
//                    ichyxDao.changeJdbcTemplate(CommonConstant.YX);
//                    ichyxDao.addCheckDetail(headList, result.subList(m*1000, (m + 1)*1000 > result.size() ? result.size() : (m+1)*1000));
//                }
            }
            bufferedWriter.flush();
            bufferedWriter.close();

        }catch (Exception e){
            e.printStackTrace();
        }*/
        /*String test = "'2011/10/5 21:54','2011/10/5 11:00','2011/10/2 7:55','2011/9/20 12:28','2011/10/1 20:36','2011/9/28 14:46','2011/9/4 7:55','2011/9/24 17:28','2011/9/7 15:18','2011/9/5 15:26','2011/9/3 9:35','2011/9/2 13:54','2011/9/1 19:56','2011/9/1 19:56','2011/9/1 19:56','2011/9/1 19:55','2011/10/11 18:14','2011/9/1 19:33','2011/9/23 8:36','2011/9/22 8:49','2011/10/10 17:44','2011/10/9 21:29','2011/10/9 21:27','2011/10/8 10:37','2011/10/8 10:28','2011/10/4 8:23','2011/10/4 4:30','2011/10/4 4:16','2011/10/4 4:16','2011/10/1 20:44','2011/10/1 20:42','2011/9/21 20:46','2011/10/8 10:34','2011/10/8 10:32','2011/10/8 10:30','2011/9/21 10:52','2011/9/20 23:32','2011/9/20 10:54','2011/9/20 10:51','2011/9/14 10:17','2011/9/20 10:27','2011/9/7 15:11','2011/9/18 7:51','2011/9/16 8:26','2011/9/14 10:21','2011/9/14 10:12','2011/9/14 8:38','2011/9/8 10:35','2011/9/7 15:22','2011/9/27 8:36','2011/9/26 9:44','2011/9/26 9:39'";
        String[] testArr = test.split(",");
        List<String> timeList = new ArrayList<>();
        for(int i = 0; i < testArr.length; i++){
            //System.out.println(testArr[i].replaceAll("'", ""));
            timeList.add(testArr[i].replaceAll("'", ""));
        }
        Collections.sort(timeList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
//                        Long firstCreateDate = TimeUtil.dateStringToLong((String)o1.get(CommonConstant.CREATE_DATE));
//                        Long secondCreateDate = TimeUtil.dateStringToLong((String)o2.get(CommonConstant.CREATE_DATE));
                Long firstCreateDate = TimeUtil.txtDateStringToLong(o1);
                Long secondCreateDate = TimeUtil.txtDateStringToLong(o2);
                long result;
                if(firstCreateDate.longValue() == secondCreateDate){
                    result = 0;
                }else{
                    result =  (firstCreateDate - secondCreateDate);
                }
                if(result != (int)result)
                    System.out.println(o1 + " " + o2 + " " + result + " " + (int)result);
                return (int)result;
            }
        });
        File file = new File(("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾/糖尿病病历"));
        File[] childFileArr = file.listFiles();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < childFileArr.length; i++){
            if(childFileArr[i].getName().startsWith("32522265")){
                InputStreamReader isr = new InputStreamReader(new FileInputStream(childFileArr[i]), "GBK");
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
            }

        }
        System.out.println(sb.toString());*/
        String medicalContent = "2016-03-11 10:20            　　首次病程记录  　胡维银，男，90岁，已婚，汉族，上海杨浦区人，家住上海市杨浦区嫩江路863号907室。因“双下肢静息痛2个月”门诊拟“下肢动脉硬化闭塞症”于2016-03-11收入我科。病例特点：1、老年男，慢性起病，治疗史明确，病程较长。2、患者于2个月前无明显诱因逐渐出现双下肢静息痛。无法下地行走，感双下肢发冷，无破溃、坏疽。其他疾病情况：高血压、糖尿病病史30余年。服药情况：阿司匹林肠溶片、硝苯地平控释片、依帕司他片、阿卡波糖片、瑞格列奈片、培元通脑胶囊、门冬胰岛素30注射液。3、查体：T 36.0℃，P 80bpm，R 20bpm，BP 138／80mmHg。双下肢无畸形，双足皮肤色泽发绀，双下肢皮温低。双侧足背动脉未触及，右侧胫后动脉弱，左侧胫后动脉未触及，双侧股动脉搏正常。双下肢感觉功能未见明显异常，活动可。双侧桡动脉、颈动脉搏动无明显异常。4、辅助检查：2016-3-1来我院就诊，门诊行双下肢彩超示1.双侧股动脉内膜增厚毛糙，伴多发斑块形成；2.双侧股动脉、腘动脉不全性闭塞，双侧股浅动脉闭塞。深静脉血栓评估：(1-2分)低危    　　　       根据上述病史特点：初步诊断：1.下肢动脉硬化闭塞症（静息痛期） 2.糖尿病 3.高血压诊断依据：下肢动脉硬化闭塞症（静息痛期）诊断依据：因双下肢静息痛2个月入院。查体双下肢缺血表现明显。糖尿病诊断依据：糖尿病病史30余年，胰岛素皮下注射控制血糖。高血压诊断依据：高血压病史30余年，长期服用降压药物控制血压。鉴别诊断：１、血栓闭塞性脉管炎（TAO）：多见于青壮年男性，病因尚未明确，常有吸烟及受凉史，病变多累及双侧下肢中小动脉，常伴有游走性浅静脉炎。患者为老年男性，病变动脉为主干，与血栓闭塞性脉管炎不符。 2、急性动脉栓塞：多急性起病且进展迅速。多源自于心脏源性栓子，患者多伴随有房颤，且未规律华发林化。行下肢动脉造影（ＣＴＡ、ＭＲＡ、ＤＳＡ）或彩超可明确。诊疗计划：１.检查安排：完善三大常规、肝肾功能、凝血、下肢动脉CTA，心脏彩超、心电图及胸片等检查。２.治疗计划：继续控制血压、血糖，排除手术禁忌，择期行双下肢PTA+支架成形术。本病例按照临床路径计划实施：是３.预期的治疗结果：缓解病程进程、减轻病人痛苦 ４.预计住院时间：7天。５.预计治疗费用：70000元（以实际发生费用为准）。袁良喜/李强2016-03-12 08:08　　　　　袁良喜主治医师首次查房记录    今日袁良喜主治医师查房：补充的病史和体征：无。今日患者一般情况可，生命体征：体温36.5℃，脉搏76次／分，呼吸18次／分，血压142／80mmHg，神志清，精神可，睡眠可，饮食可，大小便正常。主治医师查房后指出：主治医师48小时诊断：1.下肢动脉硬化闭塞症（静息痛期） 2.糖尿病 3.高血压。诊断依据：　　下肢动脉硬化闭塞症（静息痛期） 诊断依据：下肢缺血症状明显，双下肢皮温低，足背动脉搏动未及。　　糖尿病诊断依据：病史30余年，皮下注射胰岛素及口服降糖药物治疗。　　  高血压诊断依据：病史30余年，口服降压药控制血压。鉴别诊断：1、急性动脉栓塞：多急性起病且进展迅速。多源自于心脏源性栓子，患者多伴随有房颤，且未规律华发林化。行下肢动脉造影（ＣＴＡ、ＭＲＡ、ＤＳＡ）或彩超可明确。   2、腰椎间盘突出症、椎管狭窄等：也可表现为行走后下肢疼痛，多由弯腰等活动所致，行走距离可变，下肢动脉搏动可触及，与本例不符。治疗计划：完善必要的辅助检查，排除手术禁忌，择期行下肢PTA备支架成形术。袁良喜/李强2016-03-13 08:59　　　　　包俊敏主诊医师首次查房记录   今日包俊敏主诊医师查房后分析病情如下：补充的病史和体征：无。查体：双下肢无畸形，双足皮肤色泽发绀，双下肢皮温低。双侧足背动脉未触及，右侧胫后动脉弱，左侧胫后动脉未触及，双侧股动脉搏正常。双下肢感觉功能未见明显异常，活动可。双侧桡动脉、颈动脉搏动无明显异常。颈动脉、椎动脉彩超示右侧颈内动脉闭塞。今日患者一般情况可，生命体征：体温36℃，脉搏80次／分，呼吸18次／分，血压110／70mmHg，神志清，精神可，睡眠可，饮食可，大小便正常。诊断、病情、治疗方法分析讨论：患者目前诊断为1.下肢动脉硬化闭塞症（静息痛期） 2.糖尿病 3.高血压。合并有右侧颈内动脉闭塞，有手术指征，向家属讲明，择期予以手术。注意事项：待双下肢CTA结果，择期手术。袁良喜/李强2016-03-15 11:11　　　　　术前小结简要病情：患者胡维银，男，90岁，已婚，汉族，上海杨浦区人。因“双下肢静息痛2个月”门诊拟“下肢动脉硬化闭塞症”于2016-03-11收入我科。查体：双下肢无畸形，双足皮肤色泽发绀，双下肢皮温低。双侧足背动脉未触及，右侧胫后动脉弱，左侧胫后动脉未触及，双侧股动脉搏正常。双下肢感觉功能未见明显异常，活动可。双侧桡动脉、颈动脉搏动无明显异常。辅助检查2016-3-1来我院就诊，门诊行双下肢彩超示1.双侧股动脉内膜增厚毛糙，伴多发斑块形成；2.双侧股动脉、腘动脉不全性闭塞，双侧股浅动脉闭塞。根据以上病史、查体及辅助检查结果，可初步诊断为1.下肢动脉硬化闭塞症（静息痛期）2.糖尿病3.高血压。经与家属谈话并签字同意定于2016年03月16日在局麻下行下肢动脉造影备PTA支架成形备置管溶栓术治疗。术前术者查看患者及病情评估：包俊敏主诊医师术前查看患者，目前诊断明确，血糖、血压控制可，双下肢缺血症状明显，影像提示右下肢长段股浅动脉闭塞，膝下动脉未显影，左下肢胫后动脉显影，拟于明日先行在局麻下行右下肢动脉造影+PTA支架成形术；左下肢缺血症状可行限期手术治疗。术前诊断：1.下肢动脉硬化闭塞症（静息痛期）2.糖尿病3.高血压。手术指征：双下肢缺血症状明显，影像证实下肢动脉硬化闭塞症。手术禁忌症:无。拟施手术名称和手术方式：右下肢动脉造影+PTA支架成形术。手术时间：2016年03月16日拟施麻醉方式：局麻注意事项：１、术前完善检查，做好术前评估；２、术中操作细致轻柔，避免副损伤；３、术后加强护理，注意穿刺点有无出血。袁良喜/董健2016-03-15 16:16　　　　　术前讨论讨论日期：2016年03月15日参加讨论人员：包俊敏主任医师、袁良喜主治医师、董健住院医师、李强进修医师、李海燕护士长或责任护士及相关科室人员主持人：包俊敏主诊医师讨论内容：　　李强进修医师：病史汇报详见入院记录。患者因双下肢静息痛2个月入院。根据病史、查体及辅助检查结果初步诊断为1.下肢动脉硬化闭塞症（静息痛期） 2.糖尿病 3.高血压。术前准备已完善。经与家属谈话并签字同意定于2016年03月16日在全麻下行右下肢动脉造影+PTA支架成形术治疗。    袁良喜主治医师：患者术前诊断1.下肢动脉硬化闭塞症（静息痛期） 2.糖尿病 3.高血压。手术指征双下肢缺血症状明显，影像证实下肢动脉硬化闭塞症。手术禁忌症无。拟实施右下肢动脉造影+PTA支架成形术，拟实施局部麻醉。手术可能存在的风险术中使用造影剂致过敏及急性肾功能衰竭等，严重者可危及生命。术中损伤血管、神经，术中动脉破裂出血，失血性休克，术后假性动脉瘤形成，术后穿刺点出血。防范措施：术中仔细操作，术后注意应用抗凝药物，监测生命体征，观察出血点情况。    李海燕护士长（或责任护士）：术后严密观察，注意有无穿刺点出血及患肢血运情况。    包俊敏主任医师总结：患者目前诊断下肢动脉硬化闭塞症（静息痛期）诊断明确，合并高血压、糖尿病，目前下肢缺血症状明显，静息痛严重，需止痛药物治疗，经讨论拟行右下肢动脉造影+PTA支架成形术。术前完善检查，；术中操作仔细，选择合适球囊、支架；术后予以抗凝、抗血小板、扩张血管治疗。袁良喜/李强2016-03-16 16:40　　　　　术后首次病程记录    患者今日在局麻下行下肢动脉造影PTA支架成形术。术中诊断1.下肢动脉硬化闭塞症（静息痛期）2.糖尿病3.高血压。手术经过如下：穿刺后造影提示左肾动脉稍狭窄，右肾动脉通畅、管径正常，腹主动脉、双侧髂动脉通畅，管径正常、股总动脉通畅，股深动脉通畅，侧枝发达，股浅动脉自开口处以下至中段均不显影，股浅动脉下段及腘动脉由侧枝供血，局限性狭窄，但血流通畅；腓动脉多发狭窄，胫前动脉通畅，但中段有一小动静脉瘘。导管配合导丝，尝试开通股浅动脉闭塞段未成功。遂消毒腘窝，逆行穿刺腘动脉。导丝成功通过股浅动脉闭塞段。在股动脉入路导管配合下，导丝头端通过导管引出体外。退出导管，自股动脉沿导丝送入5mm*22cm球囊于股浅动脉，对股浅动脉闭塞段进行扩张。然后退出腘动脉处鞘管，沿股动脉送入4*120mm球囊对腘动脉进行扩张5min，并进行加压包扎。然后先后送入5*150mm和6*170mm支架于腘动脉和股浅动脉闭塞段，透视下精确定位后成功释放。经后扩后再次造影显示股浅动脉、腘动脉全段通畅，血流迅速，支架形态良好，位置恰当，腘动脉周围无造影剂渗出。术后予抗凝止痛扩血管等治疗。    术后深静脉血栓评估：(3-4分)中危袁良喜2016-03-17 10:40　　　　　术后第一天记录    术后第1天，患者生命体征：体温37.0℃，脉搏70次／分，呼吸19次／分，血压130／75mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况穿刺点无渗出，患肢皮温较前温暖。袁良喜主治医师查房指示：患者术后一般情况可，生命体征平稳，继续当前治疗方案，注意观察患肢血供。袁良喜/董健2016-03-18 08:57　　　　　术后第二天记录    术后第2天，患者生命体征：体温37.3℃，脉搏72次／分，呼吸18次／分，血压146／80mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况心肺未及异常，右下肢股动脉及腘动脉穿刺点无渗出，小腿轻度肿胀，患肢皮温较前温暖。袁良喜主治医师查房指示：患者术后恢复可，已停用丹参、疏血通，加用西洛他唑片(100.0000mg);2/日;口服，继续观察患肢血运。袁良喜/李强2016-03-19 15:43　　　　　术后第三天记录术后第3天，患者生命体征：体温36.7℃，脉搏70次／分，呼吸19次／分，血压130／75mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况同前。辅助检查2016-3-19 12:44:47  血  钠139mmol/L、钾4.4mmol/L、氯103mmol/L、肌酐136umol/L2016-3-19 12:21:28  血  白细胞计数12.33x10^9/L、中性粒细胞计数10.66x10^9/L、红细胞计数3.98x10^12/L、血小板计数158x10^9/L、血红蛋白118g/L。包俊敏主诊医师查房指示：术后一般情况可，生命体征平稳，继续当前治疗方案，注意观察患者生命体征变化。袁良喜2016-03-21 11:07　　　　　袁良喜主治医师查房记录    今日袁良喜主治医师查房，患者主诉:无。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：体温36.5℃，脉搏70次／分，呼吸18次／分，血压130/70mmHg。查体情况:患肢皮温暖，腓肠肌轻压痛，穿刺点无渗出。辅助检查结果：2016-3-21 7:04:53  血  白细胞计数8.10x10^9/L、中性粒细胞计数6.58x10^9/L、红细胞计数3.49x10^12/L、血小板计数186x10^9/L、血红蛋白103g/L；2016-3-20 12:11:27  血  血清降钙素原0.436ng/ml。袁良喜主治医师查房后指示：患者术后一般情况可，生命体征平稳，下肢血供改善明显，明日可出院，出院后3个月门诊复诊。袁良喜";
        //medicalContent = processMedicalContent(medicalContent);
        System.out.println(medicalContent);
        //System.out.println(sb.toString());
        Matcher matcher = PatternUtil.MEDICAL_CONTENT_TXT_SPLIT_PATTERN.matcher(medicalContent);
        int matchCount = 0;
        Map<Long, String> timeContentMap = new TreeMap<>();
        String lastTime = null;
        int lastIndex = 0;
        while(matcher.find()){
            String time = matcher.group();
            time = time.substring(0, time.length() - 1);
            if(lastTime != null){
                String content = medicalContent.substring(medicalContent.indexOf(lastTime,lastIndex) + CommonConstant.MEDICAL_CONTENT_TXT_SPLIT_PATTERN_LENGTH, medicalContent.indexOf(time, medicalContent.indexOf(lastTime,lastIndex) + 1));
                lastIndex = medicalContent.indexOf(lastTime, lastIndex) + 1;
                long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
                while(timeContentMap.containsKey(key)){
                    key++;
                }
                //System.out.println(content);
                timeContentMap.put(key, content);
            }
            lastTime = time;
            matchCount++;
        }
        if(lastTime != null) {
            long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
            while(timeContentMap.containsKey(key)){
                key++;
            }
            timeContentMap.put(key, medicalContent.substring(medicalContent.indexOf(lastTime, lastIndex) + CommonConstant.MEDICAL_CONTENT_TXT_SPLIT_PATTERN_LENGTH));
        }
        for(Long key: timeContentMap.keySet()){
            System.out.println(timeContentMap.get(key));
        }
        System.out.println(matchCount);
    }
}
