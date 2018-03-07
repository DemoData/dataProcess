package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.constant.CommonConstant;
import com.example.demo.dao.ICHYXDao;
import com.example.demo.service.IMedicalContentSplitService;
import com.example.demo.util.PatternUtil;
import com.example.demo.util.StringUtil;
import com.example.demo.util.TimeUtil;
import com.example.model.MedicalContentSplitModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;

@Service
public class MedicalContentSplitServiceImpl implements IMedicalContentSplitService{

    @Autowired
    private ICHYXDao ichyxDao;



    @Override
    public boolean medicalContentSplit(){
        try {
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
            for(Map<String, Object> entity : medicalContentCountMap){
                //保存拆分的内容对应的时间
                Map<String, String> contentTimeMap = new HashMap<>();
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(entity));
                String medicalContent = jsonObject.getString(CommonConstant.MEDICAL_CONTENT);
                Integer num = jsonObject.getIntValue("num");
                String visitNumber = jsonObject.getString(CommonConstant.A_VISIT_NUMBER);
                Matcher matcher = PatternUtil.MEDICAL_CONTENT_SPLIT_PATTERN.matcher(medicalContent);
                int matchCount = 0;
                Map<Long, String> timeContentMap = new TreeMap<>();
                String lastTime = null;
                int lastIndex = 0;
                while(matcher.find()){
                    String time = matcher.group();
                    time = time.substring(0, time.length() - 1);
                    if(lastTime != null){
                        String content = medicalContent.substring(medicalContent.indexOf(lastTime,lastIndex) + CommonConstant.MEDICAL_CONTENT_SPLIT_PATTERN_LENGTH, medicalContent.indexOf(time, medicalContent.indexOf(lastTime,lastIndex) + 1));
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
                    String content = medicalContent.substring(medicalContent.indexOf(lastTime, lastIndex) + CommonConstant.MEDICAL_CONTENT_SPLIT_PATTERN_LENGTH);
                    timeContentMap.put(key, content);
                    contentTimeMap.put(StringUtil.trim(content), lastTime);
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
                    fileWriter.write("数据库记录匹配条数：" + num + "\n");
                    fileWriter.write("实际匹配条数:" + matchCount + "\n");
                    fileWriter.write("medicalContent:" + medicalContent + "\n");
                    notSplitCount++;
                    //return false;
                }else{
                    sum += matchCount;
                }
                List<String> contentList = new ArrayList<>();
                List<Integer> contentUseFlagList = new ArrayList<>();
                for(Long key : timeContentMap.keySet()){
                    contentList.add(StringUtil.trim(timeContentMap.get(key)));
                    contentUseFlagList.add(0);
                }
                List<Map<String, Object>> createDateMedicalNameMap = ichyxDao.findCreateDateMedicalNameMapByVisitNumberAndMedicalContent(visitNumber, medicalContent);
                if(createDateMedicalNameMap.size() != contentList.size()){
                    System.out.println("查找数据有误" + createDateMedicalNameMap.size() + " " + contentList.size());
                }
                Collections.sort(createDateMedicalNameMap, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Long firstCreateDate = TimeUtil.dateStringToLong((String)o1.get(CommonConstant.CREATE_DATE));
                        Long secondCreateDate = TimeUtil.dateStringToLong((String)o2.get(CommonConstant.CREATE_DATE));
                        return (int)(firstCreateDate - secondCreateDate);
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
                            singleMap = ichyxDao.findByVisitNumberAndMedicalContentLimitOne(visitNumber, medicalContent);
                        }
                        String content = contentList.get(m);
                        Map<String, Object> addEntity = new HashMap<>();
                        addEntity.putAll(singleMap);
                        addEntity.remove("id");
                        addEntity.put(CommonConstant.UPDATE_CONTENT, content);
                        addEntity.put(CommonConstant.CREATE_DATE, TimeUtil.medicalContentTimeFormat(contentTimeMap.get(content)));
                        addEntity.put(CommonConstant.RECORD_DATE, addEntity.get(CommonConstant.CREATE_DATE));
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
            for(MedicalContentSplitModel medicalContentSplitModel : forbidList){
                fileWriter.write("禁用行:" + medicalContentSplitModel + "\n");
            }
            for(Map<String, Object> entity : addList){
                fileWriter.write("添加行:" + entity + "\n");
            }
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

    public static void main(String[] args) {
        List<Integer> a = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            a.add(i);
        }
        a.set(1, 2);
        for(int i = 0; i < a.size(); i++){
            System.out.println(a.get(i));
        }
        System.out.println("术后第1\u007F\u007F\u007F天，患者生命体征平稳，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体：体温38.2℃".startsWith("术后第一天记录"));
        String medicalContent = "2015-03-0417:26首次病程记录　顾彩亚，女，67岁，已婚，汉族，江苏省人，家住江苏省太仓市沙溪镇直塘光明路129号。因“剑突下不适1月余”门诊拟“胰腺占位”于2015-03-04收入我科。病例特点：1、老年女，慢性起病，治疗史明确，病程较长。2、患者1月前无明显诱因下出现饭后剑突下不适，无发热恶寒、恶心呕吐、腹痛腹泻、心悸胸痛，2015-02-23就诊当地医院予抑酸护胃等对症治疗效果不佳，进一步MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。近日患者尿色逐渐加深，无陶土色大便，今为进一步治疗来我院就诊，门诊以\"胰腺占位\"收入院。3、查体：T36.5℃，P80bpm，R18bpm，BP130／80mmHg。腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。4、辅助检查：2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。根据上述病史特点：初步诊断：1.胰腺占位2.乙型肝炎表面抗原携带者诊断依据：1.胰腺占位诊断依据：患者1月前无明显诱因下出现饭后剑突下不适，当地医院予抑酸护胃等对症治疗效果不佳，进一步MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。近日患者尿色逐渐加深，无陶土色大便。2.乙型肝炎表面抗原携带者诊断依据：既往史明确。鉴别诊断：1.慢性胰腺炎：可有腹痛、腹泻、血糖升高等临床表现，B超、CT等提示胰腺钙化、胰管结石、胰腺假性囊肿等影像学表现。可查CA199、超声内镜＋FNA等排除。2.胆管癌：多伴有上腹部不适或者疼痛，食欲不振，消瘦，乏力，黄疸，一般多为进行性加重，可出现胆管炎性的征象，可伴瘙痒、陶土样便，辅助检查提示血清胆红素和碱性磷酸酶升高，CT，MRI，MRCP检查可见肝内胆管扩张，肝门团状弱回声，PTC，ERCP可见胆管内变化。诊疗计划：１.检查安排：进一步完善入院常规拟定下步治疗。２.治疗计划：等待辅助检查结果。本病例按照临床路径计划实施：否３.预期的治疗结果：缓解病程进程、减轻病人痛苦４.预计住院时间：7天。５.预计治疗费用：20000元（以实际发生费用为准）。丁娴/吴斌2015-03-0508:39　　　　　丁娴主治医师首次查房记录今日丁娴主治医师查房：补充的病史和体征：无；今日患者病情稳定，一般情况可，生命体征：体温37.0℃，脉搏78次／分，呼吸18次／分，血压125／80mmHg，神志清，精神可，睡眠可，饮食可，大小便正常。主治医师查房后指出：主治医师48小时诊断：1.胰腺占位2.乙型肝炎表面抗原携带者。诊断依据：1.胰腺占位诊断依据：患者1月前无明显诱因下出现饭后剑突下不适，当地医院予抑酸护胃等对症治疗效果不佳，进一步MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。近日患者尿色逐渐加深，无陶土色大便。2.乙型肝炎表面抗原携带者诊断依据：既往史明确。鉴别诊断：1.消化性溃疡：腹痛多位于中上腹，有季节性、规律性、周期性等特点，多伴有反酸、嗳气等症状，溃疡病出现幽门梗阻时，腹痛变为持续性胀痛，无节律性，常伴有呕吐，呕吐物多为隔餐宿食。确诊需依靠X线钡餐检查或胃镜，尤其是胃镜的诊断价值最大。胃镜可直接发现病变，并明确其大小、形态、数量，并可做病理活检，有助于同胃癌鉴别。幽门螺杆菌阳性则更支持消化性溃疡的诊断。2.胆总管肿瘤：患者可有慢性腹痛，黄疸伴肝功能损害，B超常提示胆总管扩张，与患者不符，可排除。治疗计划：逆行细针穿刺活检明确胰腺占位病理。丁娴/吴斌2015-03-0508:44　　　　　王美堂主诊医师首次查房记录今日王美堂教授查房后分析病情如下：补充的病史和体征：无。2015-3-5白细胞计数2.86已复x10^9/L、中性粒细胞60.8%、血小板计数218x10^9/L、血红蛋白95已复g/L；白蛋白35g/L、总胆汁酸59.0umol/L、丙氨酸氨基转移酶380u/L、门冬氨酸氨基转移酶556u/L、碱性磷酸酶1576U/L、γ-谷氨酰转肽酶1412U/L、总胆红素79.1umol/L、直接胆红素61.3umol/L、前白蛋白79mg/L；高密度脂蛋白胆固醇0.88mmol/L、低密度脂蛋白胆固醇3.69mmol/L、载脂蛋白A10.81g/L、氯104mmol/L；尿白细胞75.0/ul、尿胆原150.0umol/L、胆红素8.5umol/L、白细胞计数(仪器)59.30/ul、白细胞计数(仪器)10.7个/HP；红细胞沉降率73mm/H；甲胎蛋白(AFP)1.11ng/ml、糖链抗原125(CA125)11.80U/ml；糖类抗原CA19942.98U/ml、癌胚抗原1.25ng/ml；血浆D-二聚体1.27μg/ml、纤维蛋白原4.78g/L、凝血酶时间20.8s。今日患者病情稳定，一般情况可，生命体征：体温37℃，脉搏78次／分，呼吸18次／分，血压125／80mmHg，神志清，精神可，睡眠可，饮食可，大小便正常。诊断、病情、治疗方法分析讨论：患者以饭后剑突下不适起病，病程1月，MRI示胰头占位，MT待排。超声胃镜示胰颈部占位（Ca可能）。近日患者尿色逐渐加深，无陶土色大便。结合患者病史、实验室结果现考虑1.胰腺占位胰腺癌可能大2.乙型肝炎表面抗原携带者。为进一步明确诊断欲行EUS-FNA术治疗。注意事项：关注患者术后腹痛、出血等不适。丁娴/吴斌2015-03-0514:53　　EUS-FNA术前小结　　　简要病情：患者顾彩亚，女，67岁，已婚，汉族，江苏省人，家住江苏省太仓市沙溪镇直塘光明路129号。因“剑突下不适1月余”门诊拟“1.胰腺占位2.乙型肝炎表面抗原携带者”于2015-03-04收入我科。患者1月前无明显诱因下出现饭后剑突下不适，无发热恶寒、恶心呕吐、腹痛腹泻、心悸胸痛，2015-02-23就诊当地医院予抑酸护胃等对症治疗效果不佳，进一步MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。近日患者尿色逐渐加深，无陶土色大便，今为进一步治疗来我院就诊，门诊以\"胰腺占位\"收入院。查体：腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。辅助检查2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。根据以上病史、查体及辅助检查结果，可初步诊断为1.胰腺占位2.乙型肝炎表面抗原携带者。经与家属谈话并签字同意定于2015年03月05日在局麻下行EUS+FNA治疗。术前术者查看患者及病情评估：患者病情平稳，一般情况可，无手术禁忌症。术前诊断：1.胰腺占位2.乙型肝炎表面抗原携带者。手术指征：2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。手术禁忌症:无。拟施手术名称和手术方式：EUS+FNA。手术时间：2015年03月05日拟施麻醉方式：局麻注意事项：１、术前完善相关术前检查，排除手术禁忌证，向患者及家属交代病情并签署知情同意书；２、术中规范、仔细、轻柔操作，避免术中出血、穿孔的发生；３、术后密切观察病情变化，观察有无发热、腹痛、恶心、呕血、黑便等症状。丁娴/吴斌2015-03-0514:55　　　　　讨论日期：2015年03月05日参加讨论人员：王美堂主任医师、王凯旋主任医师、丁娴主治医师、吴斌住院医师、许开云护士长及相关科室人员主持人：王美堂主诊医师讨论内容：　　吴斌住院医师：病史汇报详见入院记录。根据病史、查体及辅助检查结果初步诊断为1.胰腺占位2.乙型肝炎表面抗原携带者。经与家属谈话并签字同意定于2015年03月05日在局麻下行EUS+FNA治疗。丁娴主治医师：患者术前诊断1.胰腺占位2.乙型肝炎表面抗原携带者。手术指征：2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。手术禁忌症无。拟实施EUS+FNA，拟实施局麻。手术可能存在的风险：术中消化道出血、穿孔、术后急性胰腺炎等。防范措施：术前完善相关术前检查，明确有无手术禁忌证，术中仔细轻柔操作。王凯旋主任医师：患者病情平稳，一般情况可，无手术禁忌症。许开云护士长：患者相关术前准备已完成，注意在术前予以补液，防止患者产生低血糖，术后密切观察病人血压变化，观察患者有无腹痛、发热等症状。王美堂主任医师总结：同意丁娴主治医师、王凯旋主任医师意见，患者可行EUS+FNA缓解病情，减轻痛苦。术前完善相关术前检查，排除手术禁忌证，向家属及患者交代病情并签署知情同意书；术中规范、仔细、轻柔操作，彻底止血。术后密切观察病人病情变化，注意观察患者有无发热、腹痛等症状。丁娴/吴斌2015-03-0517:06　　　　　术后首次病程记录患者今日在局麻下行EUS-guidedFNA术。术中诊断胰颈部占位；胆总管泥沙样结石。手术经过如下：超声见胰颈部3.26*2.18cm的低回声区，内部回声不均匀，边界不清晰，脾静脉受侵犯，近端胰管扩张。胆总管扩张内可见较多点状回声；在超声内镜引导下以ECHO-22内镜超声专用穿刺针刺入胰颈部低回声病灶，抽出组织条，共数条并送病理。术后予奥曲肽抑酶治疗。密切关注患者术后腹痛不适。丁娴/吴斌2015-03-0609:12　　　　　术后第一天丁娴主治医师查房记录术后第1天，患者生命体征：体温37.0℃，脉搏78次／分，呼吸18次／分，血压128／75mmHg，患者一般情况良好，术后时有恶心感，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。辅助检查结果2015-3-5急诊淀粉酶78U/L；白细胞计数3.49x10^9/L、血小板计数233x10^9/L、血红蛋白104g/L；急诊淀粉酶54U/L。丁娴主治医师查房指示：密切关注患者术后生命体征及时处理。丁娴/吴斌2015-03-0709:33　　　　　术后第二天王美堂教授查房记录术后第2天，患者生命体征：体温36.5℃，脉搏70次／分，呼吸19次／分，血压84／49mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况腹平坦，无腹壁静脉曲张，腹部柔软，上腹部轻压痛，无反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。王美堂主诊医师查房后指示：患者目前病情稳定，今治疗不变，继续观察患者病情变化。丁娴/吴斌2015-03-0808:55　　　　　术后第三天记录术后第3天，患者生命体征：体温37℃，脉搏75次／分，呼吸18次／分，血压105／56mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体同前。今日嘱患者逐步调整饮食。继观。丁娴2015-03-0909:02　　　　　丁娴主治医师查房记录今日丁娴主治医师查房，患者主诉:胃脘部偶有隐痛。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食较少，大小便无异常。查体：体温37.0℃，脉搏78次／分，呼吸18次／分，血压130/85mmHg。查体情况:腹平坦，无腹壁静脉曲张，腹部柔软，上腹部轻压痛，无反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。明日行胰腺增强CT检查明确肿块与周围组织血管的关系。继观。丁娴/李世峰2015-03-1009:13　　　　　王美堂主诊医师查房记录　　今日王美堂主诊医师查房，患者主诉:胃脘部偶有隐痛。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食较少，大小便无异常。查体：体温36.8℃，脉搏72次／分，呼吸18次／分，血压125/75mmHg。查体情况:腹平坦，无腹壁静脉曲张，腹部柔软，上腹部轻压痛，无反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。接回报：2015-03-10胰腺CT：胆总管壁结节，伴上游胆管扩张，考虑胆总管癌可能；胰颈前方多枚肿大淋巴结，并侵犯胰颈部致胰管扩张；胆囊结石。等待病理回报。继观。丁娴/李世峰2015-03-1109:19　　　　　住院医师病程记录患者主诉:胃脘部偶有隐痛。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食较少，大小便无异常。查体：体温36.8℃，脉搏72次／分，呼吸18次／分，血压125/75mmHg。查体情况同前。明日复查血生化。丁娴/李世峰2015-03-1208:33　　　　　丁娴主治医师查房记录今日丁娴主治医师查房，患者主诉:乏力。今日患者病情稳定，一般情况可，神志清楚，精神可，睡眠可，饮食较少，大小便无异常。查体：体温36.9℃，脉搏70次／分，呼吸18次／分，血压125/85mmHg。查体情况:腹平坦，无腹壁静脉曲张，腹部柔软，上腹部轻压痛，无反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。患者CT示胆总管扩张，今日拟行ERCP支架置入术。继观.丁娴/吴斌2015-03-1210:38　　　　　ERCP术前小结简要病情：患者顾彩亚，女，67岁，已婚，汉族，江苏省人，家住江苏省太仓市沙溪镇直塘光明路129号。因“剑突下不适1月余”门诊拟“1.胰腺占位2.乙型肝炎表面抗原携带者”于2015-03-04收入我科。患者1月前无明显诱因下出现饭后剑突下不适，无发热恶寒、恶心呕吐、腹痛腹泻、心悸胸痛，2015-02-23就诊当地医院予抑酸护胃等对症治疗效果不佳，进一步MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。近日患者尿色逐渐加深，无陶土色大便，今为进一步治疗来我院就诊，门诊以\"胰腺占位\"收入院。查体：腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。辅助检查2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。2015-03-10胰腺CT：胆总管壁结节，伴上游胆管扩张，考虑胆总管癌可能；胰颈前方多枚肿大淋巴结，并侵犯胰颈部致胰管扩张；胆囊结石。根据以上病史、查体及辅助检查结果，可初步诊断为1.胰腺占位2.乙型肝炎表面抗原携带者。经与家属谈话并签字同意定于2015年03月12日在局麻下行ERCP术治疗。术前术者查看患者及病情评估：患者病情平稳，一般情况可，无手术禁忌症。术前诊断：1.胰腺占位2.乙型肝炎表面抗原携带者。手术指征：2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。2015-03-10胰腺CT：胆总管壁结节，伴上游胆管扩张，考虑胆总管癌可能；胰颈前方多枚肿大淋巴结，并侵犯胰颈部致胰管扩张；胆囊结石。拟施手术名称和手术方式：ERCP支架置入术。手术时间：2015年03月12日拟施麻醉方式：局麻注意事项：１、术前完善相关术前检查，排除手术禁忌证，向患者及家属交代病情并签署知情同意书；２、术中规范、仔细、轻柔操作，避免术中出血、穿孔的发生；３、术后密切观察病情变化，观察有无发热、腹痛、恶心、呕血、黑便等症状。丁娴/吴斌2015-03-1210:42　　　　　ERCP术前讨论讨论日期：2015年03月12日参加讨论人员：王美堂主任医师、王凯旋主任医师、丁娴主治医师、吴斌住院医师、许开云护士长及相关科室人员主持人：王美堂主诊医师讨论内容：　　吴斌住院医师：病史汇报详见入院记录。根据病史、查体及辅助检查结果初步诊断为1.胰腺占位2.乙型肝炎表面抗原携带者。经与家属谈话并签字同意定于2015年03月12日在局麻下行ERCP+支架置入治疗。丁娴主治医师：患者术前诊断1.胰腺占位2.乙型肝炎表面抗原携带者。手术指征：2015-02-23就诊当地医院MRI示胰头占位，MT待排。2015-03-03就诊我院超声胃镜示胰颈部占位（Ca可能）。2015-03-10胰腺CT：胆总管壁结节，伴上游胆管扩张，考虑胆总管癌可能；胰颈前方多枚肿大淋巴结，并侵犯胰颈部致胰管扩张；胆囊结石。手术禁忌症无。拟实施ERCP+支架置入，拟实施局麻。手术可能存在的风险：术中消化道出血、穿孔、术后急性胰腺炎等。防范措施：术前完善相关术前检查，明确有无手术禁忌证，术中仔细轻柔操作。王凯旋主任医师：患者病情平稳，一般情况可，无手术禁忌症。许开云护士长：患者相关术前准备已完成，注意在术前予以补液，防止患者产生低血糖，术后密切观察病人血压变化，观察患者有无腹痛、发热等症状。王美堂主任医师总结：同意丁娴主治医师、王凯旋主任医师意见，患者可行ERCP+支架置入缓解病情，减轻痛苦。术前完善相关术前检查，排除手术禁忌证，向家属及患者交代病情并签署知情同意书；术中规范、仔细、轻柔操作，彻底止血。术后密切观察病人病情变化，注意观察患者有无发热、腹痛等症状。丁娴/吴斌2015-03-1216:46　　　　　术后首次病程记录患者今日在局麻下行拟行胆管造影胆管支架置入术。术中诊断胆总管恶性狭窄。手术经过如下：于十二指肠内侧找见主乳头，乳头呈乳头型，开口呈绒毛状，循Boston自带导丝插入Boston7355切开刀，注射造影剂后，胆管显影。造影剂为碘克沙醇5ml；X片示胆总管中段狭窄，长约2cm，近端胆管轻度扩张；循导丝置入BOSTON一体式长7cm，直径10F塑料支架，支架定位良好；其后有胆汁溢入肠腔。术后予抑酶抑酸、抑酸护胃营养支持处理。注意淀粉酶、血常规回报。继观。丁娴/吴斌2015-03-1308:41　　　　　术后第一天丁娴主治医师查房记录术后第1天，患者生命体征：体温37.0℃，脉搏78次／分，呼吸18次／分，血压125／80mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。辅助检查结果2015-3-12急诊淀粉酶47U/L；白细胞计数2.70x10^9/L、中性粒细胞40.8%、血小板计数190x10^9/L、血红蛋白104g/L；高密度脂蛋白胆固醇0.65mmol/L、低密度脂蛋白胆固醇3.85mmol/L、载脂蛋白A10.56g/L、载脂蛋白B1.29g/L、尿素7.6mmol/L、肌酐49umol/L、尿酸0.08mmol/L。丁娴主治医师查房指示：患者术后病情可，密切关注患者病情变化及时处理。丁娴/吴斌2015-03-1409:02　　　　　术后第二天王美堂主诊医师查房记录术后第2天，患者生命体征：体温36.8℃，脉搏70次／分，呼吸18次／分，血压130／80mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。患者术后恢复可，今日可进食半流饮食。继观。丁娴/吴斌2015-03-1509:40　　　　　术后第三天记录术后第3天，，患者生命体征：体温37℃，脉搏72次／分，呼吸18次／分，血压135／80mmHg，患者一般情况良好，神志清，精神可，睡眠可，饮食可，大小便正常，查体情况腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。患者术后恢复可，今日出院，出院后可至专科进一步治疗。丁娴";
        Matcher matcher = PatternUtil.MEDICAL_CONTENT_SPLIT_PATTERN.matcher(medicalContent);
        int matchCount = 0;
        Map<Long, String> timeContentMap = new TreeMap<>();
        String lastTime = null;
        int lastIndex = 0;
        while(matcher.find()){
            String time = matcher.group();
            time = time.substring(0, time.length() - 1);
            if(lastTime != null){
                String content = medicalContent.substring(medicalContent.indexOf(lastTime,lastIndex) + CommonConstant.MEDICAL_CONTENT_SPLIT_PATTERN_LENGTH, medicalContent.indexOf(time, medicalContent.indexOf(lastTime,lastIndex) + 1));
                lastIndex = medicalContent.indexOf(lastTime, lastIndex) + 1;
                long key = PatternUtil.medicalContentSplitPatternToInt(lastTime);
                while(timeContentMap.containsKey(key)){
                    key++;
                }
                System.out.println(content);
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
            System.out.println(medicalContent.substring(medicalContent.indexOf(lastTime, lastIndex) + CommonConstant.MEDICAL_CONTENT_SPLIT_PATTERN_LENGTH));
            timeContentMap.put(key, medicalContent.substring(medicalContent.indexOf(lastTime, lastIndex) + CommonConstant.MEDICAL_CONTENT_SPLIT_PATTERN_LENGTH));
        }
        System.out.println(matchCount);
    }
}
