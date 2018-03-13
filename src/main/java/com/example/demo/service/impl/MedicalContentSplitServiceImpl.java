package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.constant.CommonConstant;
import com.example.demo.dao.ICHYXDao;
import com.example.demo.dao.PandianDao;
import com.example.demo.service.IMedicalContentSplitService;
import com.example.demo.util.PatternUtil;
import com.example.demo.util.StringUtil;
import com.example.demo.util.TimeUtil;
import com.example.model.MedicalContentSplitModel;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
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

    private Map<String, Integer[]> groupCount(String sql) {
        Map<String, Integer[]> result = new HashMap<>();
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
                String mapping = (String)value.get("mapping");
                Integer count = ((Long)value.get("num")).intValue();
                if(!result.containsKey(id)) {
                    Integer[] arr = new Integer[2];
                    arr[0] = 0;
                    arr[1] = 0;
                    result.put(id, arr);
                }
                if(mapping.startsWith("入院记录")){
                    result.get(id)[0] += count;
                }else if(mapping.startsWith("出院记录")){
                    result.get(id)[1] += count;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
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
                        "LINES TERMINATED by '\\n'");
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
        sql = "select count(一次就诊号) from 病历文书 where mapping like '出院记录%'";
        int chuyuancount = dataculAdd(sql);
        jsonObject.put("入院记录", ruyuancount);
        jsonObject.put("出院记录", chuyuancount);
        jsonObject.put("病历记录汇总", (ruyuancount + chuyuancount));
        jsonObject.put("住院号总数", visitNumSet.size());
        sql = "select distinct 一次就诊号 from 病历文书 where mapping like '入院记录%'";
        Set<String> ruyuan = datacul(sql);
        sql = "select distinct 一次就诊号 from 病历文书 where mapping like '出院记录%'";
        Set<String> chuyuan = datacul(sql);
        ruyuan.retainAll(chuyuan);
        jsonObject.put("单次完整出入院盘点", ruyuan.size());
        sql = "select 病人ID号,mapping,count(*) num from 病历文书 where mapping is not null group by 病人ID号,mapping";
        Map<String, Integer[]> result = groupCount(sql);
        List<String> notFoungList = new ArrayList<>();
        for(String key : result.keySet()){
            if(!userSet.contains(key)){
                notFoungList.add(key);
            }
        }
        jsonObject.put("出入院未找到患者ID列表", notFoungList);
        for(String key : userSet){
            if(!result.containsKey(key)){
                result.put(key, new Integer[]{0,0});
            }
        }
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "住院详单", "xlsx", result, new String[]{"PID", "入院记录", "出院记录"});
        return jsonObject;
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
        DBObject fieldObject = new BasicDBObject();
        fieldObject.put("patientId", true);
        fieldObject.put("groupRecordName", true);
        fieldObject.put("info.text", true);
        Query ruyuanQuery = new BasicQuery(dbObject, fieldObject);
        List<JSONObject> ruyuan = pandianDao.findCountByQuery(ruyuanQuery, "Record");
        jsonObject.put("入院记录", ruyuan.size());
        DBObject chuyuanObject = new BasicDBObject();
        chuyuanObject.put("batchNo", batchNo);
        chuyuanObject.put("recordType", "出院记录");
        Query chuyuanQuery = new BasicQuery(chuyuanObject, fieldObject);
        List<JSONObject> chuyuan = pandianDao.findCountByQuery(chuyuanQuery, "Record");
        jsonObject.put("出院记录", chuyuan.size());
        jsonObject.put("病历记录汇总", chuyuan.size() + ruyuan.size());
        Set<String> groupRecordNameSet = new HashSet<>();
        Set<String> ruyuanSet =new HashSet<>();
        Set<String> chuyuanSet = new HashSet<>();
        Map<String, String> anchorOriginalMap = new HashMap<>();
        Map<String ,Integer> anchorCountMap = new HashMap<>();
        for(JSONObject value : ruyuan){
            String groupRecordName =value.getString("groupRecordName");
            groupRecordNameSet.add(groupRecordName);
            ruyuanSet.add(groupRecordName);
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
        for(JSONObject value : chuyuan){
            String groupRecordName =value.getString("groupRecordName");
            groupRecordNameSet.add(groupRecordName);
            chuyuanSet.add(groupRecordName);
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
        jsonObject.put("住院号总数", groupRecordNameSet.size());
        ruyuanSet.retainAll(chuyuanSet);
        jsonObject.put("单次完整出入院盘点", ruyuanSet.size());
        Set<String> notFoundSet =new HashSet<>();
        Map<String, Integer[]> result =new HashMap<>();
        for(JSONObject value : ruyuan){
            String id =value.getString("patientId");
            if(!userSet.contains(id)){
                notFoundSet.add(id);
            }
            if(!result.containsKey(id)){
                result.put(id, new Integer[]{0,0});

            }
            result.get(id)[0] += 1;
        }
        for(JSONObject value : chuyuan){
            String id =value.getString("patientId");
            if(!userSet.contains(id)){
                notFoundSet.add(id);
            }
            if(!result.containsKey(id)){
                result.put(id, new Integer[]{0,0});

            }
            result.get(id)[1] += 1;
        }
        jsonObject.put("出入院未找到患者ID列表", notFoundSet);
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "锚点原文对应表(mongo)", "xlsx", anchorOriginalMap, anchorCountMap, new String[]{"锚点", "数量", "原文"});
        writer("/Users/liulun/Desktop/上海长海医院/检验科/胰腺相关组/长海检验科-王蓓蕾-临床病历/王蓓蕾", "住院详单(mongo)", "xlsx", result, new String[]{"PID", "入院记录", "出院记录"});
        return jsonObject;
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
                cell = row.createCell(1);
                cell.setCellValue(arr[0]);
                cell = row.createCell(2);
                cell.setCellValue(arr[1]);
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
                cell.setCellValue(value);
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
        try{
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
        }
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
        String medicalContent = "2011-06-09 11:13            　　首次病程记录  　朱庆如，女，72岁，已婚，汉族，安徽省人，家住上海市浦东新区芳甸路33弄10号702室。因“确诊胰腺癌5月余”门诊拟“胰腺癌”于2011-06-09收入我科。病例特点：1、老年女，慢性起病，治疗史明确，病程较长。2、患者缘于2011年1月无明显诱因出现中上腹胀，进食后明显，无恶心、呕吐，皮肤巩膜逐渐黄染，全身瘙痒，伴有浓茶样小便。1月9日于我院急诊行腹部B超诊断为\"肝内外胆管扩张\"，1月10日收住我院，CT、MRCP、EUS均提示胰腺占位，考虑胰腺癌伴胆道系统扩张。FNA液基培养找到癌细胞。2011-1-20日行ERCP并置入一5cm、8.5F塑料支架和一7cm、10F的粒子支架，粒子共10颗。2011-03-06日、03-13日、03-20日予泰欣生200mg静滴3次；2011-04-03日、04-10日、04-17日予泰欣生200mg治疗。患者1周前再次出现中上腹疼痛，呈持续性胀痛，伴恶心呕吐，呕吐物为胃内容物，无发热，查血常规WBC4.42*10^9/L,GRAN%75.5%,HGB108g/L。为进一步诊治门诊拟胰腺癌收治入院。3、查体：T 36.8℃，P 80bpm，R 20bpm，BP 120／80mmHg。神清，精神软，心肺未及异常，腹软，上腹部压痛明显，无反跳痛，肝脾肋下未及，肠鸣音正常，双下肢无水肿。4、辅助检查：（2011-06-02 我院）血常规WBC4.42*10^9/L,GRAN%75.5%,HGB108g/L。 根据上述病史特点：初步诊断：胰腺癌 。诊断依据：1、老年女，慢性起病，治疗史明确，病程较长。2、患者缘于2011年1月无明显诱因出现中上腹胀，进食后明显，无恶心、呕吐，皮肤巩膜逐渐黄染，全身瘙痒，伴有浓茶样小便，无发热、寒战。1月9日于我院急诊行腹部B超诊断为\"肝内外胆管扩张\"，1月10日收住我院，CT、MRCP、EUS均提示胰腺占位，考虑胰腺癌伴胆道系统扩张。FNA液基培养找到癌细胞。2011-1-20日行ERCP并置入一5cm、8.5F塑料支架和一7cm、10F的粒子支架，粒子共10颗。2011-03-06日、03-13日、03-20日、04-03日、04-10日、04-17日予泰欣生200mg静滴6次。3、查体：腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肝区有轻叩痛，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。鉴别诊断：１、胆总管肿瘤：患者可有慢性腹痛，黄疸伴肝功能损害，B超常提示胆总管扩张，与患者不符，可排除。2、慢性胰腺炎：可有腹痛、腹泻、血糖升高等临床表现，B超、CT等提示胰腺钙化、胰管结石、胰腺假性囊肿等影像学表现。可查CA199、超声内镜＋FNA等排除。诊疗计划： 1、按消化科护理常规，一级护理，低脂半流。2、完善相关检查。3、请示上级医师决定进一步诊治方案。胡良嗥/刘明浩2011-06-10 16:46　　　　　胡良嗥主治医师首次查房记录今日胡良嗥主治医师查房：补充的病史和体征：无特殊病史补充。今日患者病情平稳，一般情况可。主治医师查房后指出：主治医师48小时诊断：胰腺癌。诊断依据：1、老年女，慢性起病，治疗史明确，病程较长。2、患者缘于2011年1月无明显诱因出现中上腹胀，进食后明显，无恶心、呕吐，皮肤巩膜逐渐黄染，全身瘙痒，伴有浓茶样小便，无发热、寒战。1月9日于我院急诊行腹部B超诊断为\"肝内外胆管扩张\"，1月10日收住我院，CT、MRCP、EUS均提示胰腺占位，考虑胰腺癌伴胆道系统扩张。FNA液基培养找到癌细胞。2011-1-20日行ERCP并置入一5cm、8.5F塑料支架和一7cm、10F的粒子支架，粒子共10颗。2011-03-06日、03-13日、03-20日\04-03日、04-10日、04-17日予泰欣生200mg静滴6次。3、查体：腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肝区有轻叩痛，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。鉴别诊断：１、胆总管肿瘤：患者可有慢性腹痛，黄疸伴肝功能损害，B超常提示胆总管扩张，与患者不符，可排除。2、慢性胰腺炎：可有腹痛、腹泻、血糖升高等临床表现，B超、CT等提示胰腺钙化、胰管结石、胰腺假性囊肿等影像学表现。可查CA199、超声内镜＋FNA等排除。治疗计划： 1、按消化科护理常规，一级护理，低脂半流。2、完善胰腺CT及上消化道碘水造影等检查。孙畅2011-06-12 10:19　　　　　李兆申主诊医师首次查房记录今日李兆申教授查房后分析病情如下：补充的病史和体征：无特殊病史补充。今日患者病情平稳，一般情况可。CT：胰体癌并胰管扩张；肝左叶血管瘤可能大，肝右叶病灶考虑坏死；肝肾囊肿；胆囊结石，胆囊炎并肝内胆管积气扩张。诊断、病情、治疗方法分析讨论：患者胰腺癌诊断明确。注意事项：注意观察患者病情变化。孙畅2011-06-15 15:44　　　　　胡良嗥主治医师查房记录   今日胡良皞主治医师查房，患者未诉不适。今日患者病情平稳，一般情况可。查体情况:神清，心肺未见明显异常，腹平软，无压痛反跳痛。辅助检查结果：血总胆红素10.4umol/L、白蛋白30g/L、碱性磷酸酶312U/L、葡萄糖8.6mmol/L，胡良皞主治医师查房后指示：患者病情尚平稳，继续同前治疗。孙畅2011-06-18 08:55　　　　　李兆申主任医师查房记录　　今日李兆申主诊医师查房，患者述乏力。一般情况欠佳，精神一般，睡眠可，食欲减退，大小便无异常。查体情况:心肺未见异常，腹平软，无压痛反跳痛。辅助检查2011-6-16血常规：HCT36%，PLT90x10^9/L，李兆申主任医师查房后指示：患者体质虚弱，继续抗炎、营养支持等对症支持治疗，密切观察病情变化。孙畅2011-06-21 08:40　　　　　胡良嗥主治医师查房记录   今日胡良皞主治医师查房，患者乏力，一般情况欠佳，查体：神清，心肺未见明显异常，腹平软，无压痛反跳痛。胡良皞主治医师查房后指示：今日予以泰欣生治疗，继续营养支持，密切观察病情。孙畅2011-06-24 08:38　　　　　李兆申主任医师查房记录　　今日李兆申主诊医师查房，今日患者病情平稳，一般情况可。查体:神清，心肺未见明显异常，腹平软，无压痛反跳痛。李兆申主任医师查房后指示：患者病情平稳，继续营养支持治疗。孙畅2011-06-27 07:46　　　　　廖专主治医师查房记录   今日廖专主治医师查房，患者诉发热。今日患者病情平稳，一般情况可。查体情况:神清，心肺未见明显异常，腹平软，无压痛反跳痛。廖专主治医师查房后指示：患者今晨有发热，予以甲磺酸帕珠沙星、奥硝唑抗炎；吲哚美辛栓纳肛退热等对症支持治疗。孙畅2011-06-30 07:51　　　　　李兆申主诊医师查房记录　　今日李兆申主诊医师查房，患者未诉不适。今日患者病情平稳，一般情况可。查体情况:神清，心肺未见明显异常，腹平软，无压痛反跳痛。李兆申主任医师查房后指示：2011-06-28已予以泰欣生治疗，患者未诉不适，目前病情平稳，要求出院，予以同意。孙畅";
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
