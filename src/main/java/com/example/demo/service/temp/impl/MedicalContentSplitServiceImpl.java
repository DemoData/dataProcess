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
import com.example.demo.util.AnchorUtil;
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
import java.util.regex.Pattern;

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
            /*int result = forbidDatabase(forbidList);
            fileWriter.write("禁用:" + result + "\n");
            result = updateDatabase(updateResult);
            fileWriter.write("更新:" + result + "\n");
            addDatabase(addList);*/
            fileWriter.flush();
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
                                       Map<String, JSONObject> anchorOriginalMap){
        for(JSONObject value : jsonObjectList){
            String groupRecordName =value.getString("groupRecordName");
            allSet.add(groupRecordName);
            singleSet.add(groupRecordName);
            String text = value.getJSONObject("info").getString("text");
            Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(text);
            while(matcher.find()){
                String anchor = matcher.group(1);
                if(!anchorOriginalMap.containsKey(anchor)){
                    text = text.replaceAll("\n", "").replaceAll("\r", "");
                    text = text.replaceAll("【【", "\n【【");
                    JSONObject resultItem = new JSONObject();
                    resultItem.put("原文", text);
                    resultItem.put("锚点数量", AnchorUtil.countAnchorCount(text));
                    resultItem.put("RID", value.getString("_id"));
                    resultItem.put("出现次数", 1);
                    resultItem.put("记录类型", value.getString("recordType"));
                    anchorOriginalMap.put(anchor, resultItem);
                }else{
                    JSONObject jsonObject = anchorOriginalMap.get(anchor);
                    jsonObject.put("出现次数", jsonObject.getInteger("出现次数") + 1);
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
        fieldObject.put("recordType", true);
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
        Map<String, JSONObject> anchorOriginalMap = new HashMap<>();
        processJSONObjectList(ruyuan, ruyuanSet, groupRecordNameSet, anchorOriginalMap);
        processJSONObjectList(chuyuan, chuyuanSet, groupRecordNameSet, anchorOriginalMap);
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
                .addCriteria(Criteria.where("format").is("table"))
                .addCriteria(Criteria.where("deleted").is(false)), "Record"));
        //jsonObject.put("表格病历", jsonObject.getInteger("化验记录"));
        jsonObject.put("出入院未找到患者ID列表", notFoundSet);
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "锚点原文对应表(mongo)" + batchNo, "xlsx", new String[]{"锚点","出现次数", "原文","记录类型","锚点数量","RID"}, anchorOriginalMap);
        System.out.println("输出文件");
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "住院详单(mongo)" + batchNo, "xlsx", result, CommonConstant.RECORD_TYPE);
        return jsonObject;
    }

    @Override
    public void menzhenMongoPandian(String batchNo) {
        DBObject dbObject = new BasicDBObject();
        dbObject.put("batchNo", batchNo);
        Pattern pattern = Pattern.compile("^.*门诊.*$", Pattern.MULTILINE);
        dbObject.put("source", new BasicDBObject("$regex", pattern));
        dbObject.put("deleted", false);
        DBObject fieldObject = new BasicDBObject();
        fieldObject.put("patientId", true);
        fieldObject.put("source", true);
        System.out.println(pandianDao.findCountByQuery(new BasicQuery(dbObject, fieldObject), "Record"));
        List<JSONObject> resultJsonObject = pandianDao.findListByQuery(new BasicQuery(dbObject, fieldObject), "Record");
        Set<String> sourceSet = new HashSet<>();
        Map<String, Map<String, Integer>> patientSourceCountMap = new HashMap<>();
        for(JSONObject jsonObject : resultJsonObject){
            String patientId = jsonObject.getString("patientId");
            String source = jsonObject.getString("source");
            if(!sourceSet.contains(source)){
                sourceSet.add(source);
            }
            if(!patientSourceCountMap.containsKey(patientId)){
                patientSourceCountMap.put(patientId, new HashMap<>());
            }
            Map<String, Integer> sourceCountMap = patientSourceCountMap.get(patientId);
            if(!sourceCountMap.containsKey(source)){
                sourceCountMap.put(source, 0);
            }
            sourceCountMap.put(source, sourceCountMap.get(source) + 1);
        }
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "门诊详单(mongo)" + batchNo, "xlsx", patientSourceCountMap, sourceSet);
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

    private void writer(String path, String fileName,String fileType,Map<String, Map<String, Integer>> result,Set titleSet) {
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
                sheet = (Sheet) wb.createSheet("门诊详单");
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
                sheet = (Sheet) wb.createSheet("门诊详单");
            }

            //添加表头
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("PID");
            Object[] titleRow = titleSet.toArray();
            for(int i = 0;i < titleRow.length;i++){
                cell = row.createCell(i + 1);
                cell.setCellValue((String)titleRow[i]);
            }
            int rowIndex = 0;
            for(String key : result.keySet()){
                row = sheet.createRow(++rowIndex);
                cell = row.createCell(0);
                cell.setCellValue(key);
                Map<String, Integer> sourceCountMap = result.get(key);
                for(int i = 0;i < titleRow.length;i++){
                    cell = row.createCell(i + 1);
                    cell.setCellValue(sourceCountMap.get((String)titleRow[i]) == null ? 0 : sourceCountMap.get((String)titleRow[i]));
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

    public void writer(String path, String fileName,String fileType,String titleRow[],Map<String, JSONObject> anchorOriginalMap){
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
                JSONObject value = anchorOriginalMap.get(key);
                row = sheet.createRow(++rowIndex);
                cell = row.createCell(0);
                cell.setCellValue(key);
                for(int i = 1;i < titleRow.length;i++){
                    cell = row.createCell(i);
                    cell.setCellValue(value.getString(titleRow[i]));
                }
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
            filePath = "/Users/liulun/Desktop/上海长海医院/肝癌/检验报告";
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
            String path = "/Users/liulun/Desktop/上海长海医院/肝癌/检验报告";
            List<String> headList = new ArrayList<>();
            File loadData = new File("/Users/liulun/Desktop/上海长海医院/肝癌/loadData.txt");
            if(!loadData.exists()){
                loadData.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(loadData));
            File   file =  new File(path);
            File[] childFileArr = file.listFiles();
            for(File childFile : childFileArr){
                if(".DS_Store".equals(childFile.getName())){
                    continue;
                }
                List<Map<String, Object>> result = excelParse(path, headList, childFile.getName());
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
        String medicalContent = "2011-10-11 15:24            　　首次病程记录盛继贤，男，77岁，已婚，汉族，浙江省人，家住浙江省平湖市锦湖花苑26-3-202。因“肝占位术后2月3周。”门诊拟“原发性肝癌”于2011-10-11收入我科。病例特点：1、老年男，慢性起病，治疗史明确，病程较长。2、2011-07-09嘉兴人民医院AFP181.1ug/L，2011-07-11肝脏CT增强：左肝占位性病变，2.5×2.5cm，考虑肝癌，肝硬化。2011-07-25于东方肝胆医院行左肝肿瘤切除术。术后病理：1.小肝细胞癌，粗梁型，III级；2.慢性肝血吸虫病。3、入院时情况：病人精神状态良好，体力情况良好，食欲食量良好，夜间盗汗，耳鸣较重，睡眠情况差，体重无明显变化，大便干，小便频数。4、查体：全身皮肤粘膜无黄染，心肺无异常。腹平坦，中上腹有一长20cm，全腹无压痛及反跳痛，肝脾肋下未及，莫非氏征（-），移动性浊音阴性，肠鸣音正常。双下肢无浮肿。5、舌红，少苔，脉弦细。根据上述病史特点：初步诊断：    中医诊断：肝癌（肝肾阴虚）    西医诊断：1.原发性肝癌Ia期术后2.血吸虫性肝硬化3.2型糖尿病诊断依据：1.原发性肝癌Ia期术后：2011-07-09嘉兴人民医院AFP181.1ug/L，2011-07-11肝脏CT增强：左肝占位性病变，2.5×2.5cm，考虑肝癌，肝硬化。2011-07-25于东方肝胆医院行左肝肿瘤切除术。术后病理：1.小肝细胞癌，粗梁型，III级；2.慢性肝血吸虫病。           2.血吸虫性肝硬化：十几岁时患有血吸虫病，2011-07-25于东方肝胆医院行左肝肿瘤切除术。术后病理：1.小肝细胞癌，粗梁型，III级；2.慢性肝血吸虫病。            3.2型糖尿病：糖尿病史30年，目前口服诺合龙，阿卡波糖及皮下注射胰岛素。空腹血糖：6mmol/L，餐后血糖：13mmol/L。 鉴别诊断：患者术后病理诊断明确，排除鉴别诊断 辨病辨证依据：患者老年男性，或因饮食不当，或因劳累过度，而致肝肾亏虚，精液不能滋养耳目，而致耳鸣，阴液不足而致夜间汗出。舌红，少苔，脉弦细。均为肝肾阴虚之象。 诊疗计划：1、中医科二级护理，完善各项入院常规检查。                2、患者证属肝肾阴虚，治当滋养肝肾，拟方如下：       解毒方 加  肝协定方  柴胡9    香附6    女贞子12                  墨旱莲12  黄芪15   石斛15   天冬15                  麦冬15    山楂炭12 六神曲12 。１.检查安排：完善各项入院检查。２.治疗计划：予以华蟾素抗肿瘤，谷胱甘肽保肝治疗。３.预期的治疗结果：防止肿瘤复发。４.预计住院时间：10天。５.预计治疗费用：5000元（以实际发生费用为准）。孟永斌2011-10-12 10:31　　　　　刘群主治医师首次查房记录今日刘群主治医师查房：补充的病史和体征：无。今日患者病情稳定，一般情况可，生命体征平稳，神志清，精神可，睡眠可，饮食可，大小便正常。主治医师查房后指出：主治医师48小时诊断：1.原发性肝癌Ia期术后2.血吸虫性肝硬化3.2型糖尿病诊断依据：1.原发性肝癌Ia期术后：2011-07-09嘉兴人民医院AFP181.1ug/L，2011-07-11肝脏CT增强：左肝占位性病变，2.5×2.5cm，考虑肝癌，肝硬化。2011-07-25于东方肝胆医院行左肝肿瘤切除术。术后病理：1.小肝细胞癌，粗梁型，III级；2.慢性肝血吸虫病。           2.血吸虫性肝硬化：十几岁时患有血吸虫病，2011-07-25于东方肝胆医院行左肝肿瘤切除术。术后病理：1.小肝细胞癌，粗梁型，III级；2.慢性肝血吸虫病。            3.2型糖尿病：糖尿病史30年，目前口服诺和龙，阿卡波糖及皮下注射胰岛素。空腹血糖：6mmol/L，餐后血糖：13mmol/L。鉴别诊断：患者术后病理诊断明确，排除鉴别诊断。治疗计划：予以华蟾素抗肿瘤，谷胱甘肽保肝治疗。孟永斌2011-10-13 11:34　　　　　陈喆主诊医师首次查房记录今日陈喆主诊医师查房后分析病情如下：补充的病史和体征：甲胎蛋白<30μg/L。癌胚抗原2.83ng/ml、糖类抗原CA1995.20U/ml。PT13.2s、APTT37.2s。WBC3.26x10^9/L、GRAN%62.3%、RBC4.38x10^12/L、HGB137g/L、PLT98x10^9/L。总胆红素13.9umol/L、白蛋白44g/L、丙氨酸氨基转移酶26U/L、门冬氨酸氨基转移酶27U/L、碱性磷酸酶71U/L、γ-谷氨酰转肽酶81U/L。今日患者病情稳定，一般情况可，生命体征平稳，神志清，精神可，睡眠可，饮食可，大小便正常。诊断、病情、治疗方法分析讨论：患者原发性肝癌术后，此次入院行术后保肝，防治肿瘤复发治疗。注意事项：密切观察患者症状体征，如有异常及时处理。孟永斌2011-10-14 11:37　　　　　住院医师病程记录    患者诉夜间小便次数多。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。B超：肝部分切除术后，血吸虫肝硬化，胆囊继发改变，胆囊息肉，前列腺钙化灶。考虑患者有前列腺增生病史，予以请泌尿科会诊，指导用药。孟永斌2011-10-15 08:43　　　　　刘群主治医师查房记录    今日刘群主治医师查房，患者无不适主述。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。患者今日病情稳定，继续予以华蟾素抗肿瘤治疗。孟永斌2011-10-16 08:45　　　　　陈喆主诊医师查房记录　　今日陈喆主诊医师查房，患者诉头顶部多发红色皮疹。今日患者病情稳定，一般情况良好，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。陈喆主诊医师查房后指示：请皮肤科会诊后明确头顶部皮疹原因。孟永斌2011-10-17 15:50　　　　　会诊病程记录今日皮肤科会诊意见：患多年来头部反复发红色小皮疹，且局部瘙痒。查体：头部红色斑疹，脱屑。考虑脂溢性皮炎。建议：予以宁肤露外用。回报上级医生会诊意见，同意暂行治疗。孟永斌2011-10-18 08:54　　　　　住院医师病程记录今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。患者诉用宁肤露外用后头顶部皮疹较前缓解。今日继续予以宁肤露外用。孟永斌2011-10-19 11:38　　　　　刘群主治医师查房记录　　今日刘群主治医师查房，患者无不适主述。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。患者有前列腺增生史，今日于门诊泌尿外科就诊，查前列腺B超，膀胱残余尿。刘群主治医师查房后指示：今日继续予以华蟾素抗肿瘤，复方甘草酸苷保肝治疗。孟永斌/顾文婷2011-10-20 15:48　　　　　住院医师病程记录    患者无不适主述。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。患者门诊B超示：前列腺未见异常，膀胱无残余尿。患者病情稳定，治疗同前。孟永斌/顾文婷2011-10-21 11:52　　　　　陈喆主诊医师查房记录　　今日陈喆主诊医师查房，患者无不适主述。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食可，大小便无异常。查体：同前。陈喆主诊医师查房后指示：患者此次入院行华蟾素治疗，现疗程已到，治疗结束，明日予以出院。孟永斌/顾文婷";
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
