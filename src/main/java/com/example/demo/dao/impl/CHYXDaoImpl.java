package com.example.demo.dao.impl;

import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.constant.CommonConstant;
import com.example.demo.dao.ICHYXDao;
import com.example.model.MedicalContentSplitModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Repository
public class CHYXDaoImpl implements ICHYXDao {

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YXZW_TEMPLATE)
    protected JdbcTemplate yxzwJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_JKCT_TEMPLATE)
    protected JdbcTemplate jkctJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_YX_TEMPLATE)
    protected JdbcTemplate yxJdbcTemplate;

    @Autowired
    @Qualifier(MysqlDataSourceConfig.MYSQL_TNB_TEMPLATE)
    protected JdbcTemplate tnbJdbcTemplate;

    protected  JdbcTemplate validTemplate;

    @Override
    public void processTest() {
        List<Map<String, Object>> mapList = yxzwJdbcTemplate.queryForList("select distinct 病历名称 from `病历文书`");
        log.debug("=========test========");
        if (log.isInfoEnabled()) {
            log.info(mapList.toString());
        }
    }

    @Override
    public List<Map<String, Object>> findMedicalContentCountMap() {
        return validTemplate.queryForList("select 一次就诊号, 病历内容, count(*) num from 病历文书  group by 一次就诊号,病历内容 HAVING num > 1 ");
    }

    @Override
    public List<Map<String, Object>> findCreateDateMedicalNameMapByVisitNumberAndMedicalContent(String visitNumber, String medicalContent) {
        return validTemplate.queryForList("select 创建日期,病历名称 from 病历文书 where 一次就诊号 = '" + visitNumber + "' and 病历内容 = '" + medicalContent.replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'") + "'");
    }

    @Override
    public Map<String, Object> findByVisitNumberAndMedicalContentLimitOne(String visitNumber, String medicalContent) {
        return validTemplate.queryForMap("select * from 病历文书 where 一次就诊号 = '" + visitNumber + "' and 病历内容 = '" + medicalContent.replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'") + "' limit 1");
    }

    @Override
    public int update(MedicalContentSplitModel medicalContentSplitModel) {
        return validTemplate.update("update 病历文书 SET `status` = " + medicalContentSplitModel.getStatus() + ", 更新内容 = '" + medicalContentSplitModel.getMedicalContent().replaceAll("\\\\", "\\\\\\\\").replaceAll("'","\\\\'") + "' where 一次就诊号='" + medicalContentSplitModel.getVisitNumber() + "' and 创建日期='" + medicalContentSplitModel.getCreateDate() + "'");
    }

    @Override
    public int forbid(MedicalContentSplitModel medicalContentSplitModel) {
        StringBuilder sb = new StringBuilder();
        sb.append("update 病历文书 SET status = 1 where 一次就诊号 = '" + medicalContentSplitModel.getVisitNumber() + "' ");
        if(StringUtils.isNotBlank(medicalContentSplitModel.getCreateDate())){
            sb.append(" and 创建日期 = '" + medicalContentSplitModel.getCreateDate() + "' ");
        }
        if(StringUtils.isNotBlank(medicalContentSplitModel.getMedicalContent())){
            sb.append(" and 病历内容 = '" + medicalContentSplitModel.getMedicalContent().replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'") + "'");
        }
        return validTemplate.update(sb.toString());
    }

    @Override
    public void add(Map<String, Object> map) {
        StringBuilder keySb = new StringBuilder();
        StringBuilder valueSb = new StringBuilder();
        for(String key : map.keySet()){
            Object value = map.get(key);
            keySb.append("`" + key + "`");
            keySb.append(",");
            if(value instanceof String) {
                valueSb.append("'" + ((String)value).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'");
            }else{
                valueSb.append(value);
            }
            valueSb.append(",");
        }
        validTemplate.execute("insert into 病历文书(" + keySb.substring(0, keySb.length() - 1) + ") values (" + valueSb.substring(0, valueSb.length() - 1) + ")" );
    }

    @Override
    public void changeJdbcTemplate(String type) throws Exception {
        if(CommonConstant.TNB.equals(type)){
            this.validTemplate = tnbJdbcTemplate;
        }else if(CommonConstant.YXZW.equals(type)){
            this.validTemplate = yxzwJdbcTemplate;
        }else if(CommonConstant.YX.equals(type)){
            this.validTemplate = yxJdbcTemplate;
        }else if(CommonConstant.JKCT.equals(type)){
            this.validTemplate = jkctJdbcTemplate;
        }else{
            throw new Exception("类型错误");
        }
    }

    @Override
    public List<String> datacul(String sql) {
        return validTemplate.queryForList(sql, String.class);
    }

    @Override
    public Integer dataculAdd(String sql) {
        return validTemplate.queryForObject(sql, Integer.class);
    }



    @Override
    public void addCheckDetail(List<String> headList, List<Map<String, Object>> data) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into 检验报告明细(");
        for(int i = 0; i < headList.size(); i++){
            sql.append(headList.get(i));
            if(i != headList.size() - 1){
                sql.append(",");
            }else{
                sql.append(")");
            }
        }
        sql.append(" values (");
        for(int i = 0; i < headList.size(); i++){
            sql.append("?");
            if(i != headList.size() - 1){
                sql.append(",");
            }else{
                sql.append(")");
            }
        }
        validTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
               for(int m = 0; m < headList.size(); m++){
                   ps.setString(m + 1, (String)data.get(i).get(headList.get(m)));
               }
            }

            @Override
            public int getBatchSize() {
                return data.size();
            }
        });
//        for(int i = 0; i < data.size(); i++){
//            Map<String, Object> map = data.get(i);
//
//            StringBuilder keySb = new StringBuilder();
//            StringBuilder valueSb = new StringBuilder();
//            for(String key : map.keySet()){
//                Object value = map.get(key);
//                keySb.append("`" + key + "`");
//                keySb.append(",");
//                if(value instanceof String) {
//                    valueSb.append("'" + ((String)value).replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'") + "'");
//                }else{
//                    valueSb.append(value);
//                }
//                valueSb.append(",");
//            }
//            validTemplate.execute("insert into 检验报告明细(" + keySb.substring(0, keySb.length() - 1) + ") values (" + valueSb.substring(0, valueSb.length() - 1) + ")" );
//        }
    }

    @Override
    public void executeSql(String sql) {
        validTemplate.execute(sql);
    }

    @Override
    public List<Map<String, Object>> groupCount(String sql) {
        return validTemplate.queryForList(sql);
    }


    public static void main(String[] args) {
        System.out.println(("2011-06-09 11:13            　　首次病程记录  　朱庆如，女，72岁，已婚，汉族，安徽省人，家住上海市浦东新区芳甸路33弄10号702室。" +
                "因“确诊胰腺癌5月余”门诊拟“胰腺癌”于2011-06-09收入我科。病例特点：1、老年女，慢性起病，治疗史明确，病程较长。2、患者缘于2011年1月无明显诱因出现中上腹胀，进食后明" +
                "显，无恶心、呕吐，皮肤巩膜逐渐黄染，全身瘙痒，伴有浓茶样小便。1月9日于我院急诊行腹部B超诊断为\"肝内外胆管扩张\"，1月10日收住我院，CT、MRCP、EUS均提示胰腺占位，" +
                "考虑胰腺癌伴胆道系统扩张。FNA液基培养找到癌细胞。2011-1-20日行ERCP并置入一5cm、8.5F塑料支架和一7cm、10F的粒子支架，粒子共10颗。2011-03-06日、03-13日、03-20日予泰欣生200mg静" +
                "滴3次；2011-04-03日、04-10日、04-17日予泰欣生200mg治疗。患者1周前再次出现中上腹疼痛，呈持续性胀痛，伴恶心呕吐，呕吐物为胃内容物，无发热，查血常规WBC4.42*10^9/L,GRAN%75.5%,HGB108g/L。为进一步诊治门诊拟胰腺癌收治入院。3、查体：T 36.8℃，P 80bpm，R 20bpm，BP 120／80mmHg。神清，精神软，心肺未及异常，腹软，上腹部压痛明显，无反跳痛，肝脾肋下未及，肠鸣音正常，双下肢无水肿。4、辅助检查：（2011-06-02 我院）血常规WBC4.42*10^9/L,GRAN%75.5%,HGB108g/L。 根据上述病史特点：初步诊断：胰腺癌 。诊断依据：1、老年女，慢性起病，治疗史明确，病程较长。2、患者缘于2011年1月无明显诱因出现中上腹胀，进食后明显，无恶心、呕吐，皮肤巩膜逐渐黄染，全身瘙痒，伴有浓茶样小便，无发热、寒战。1月9日于我院急诊行腹部B超诊断为\"肝内外胆管扩张\"，1月10日收住我院，CT、MRCP、EUS均提示胰腺占位，考虑胰腺癌伴胆道系统扩张。FNA液基培养找到癌细胞。2011-1-20日行ERCP并置入一5cm、8.5F塑料支架和一7cm、10F的粒子支架，粒子共10颗。2011-03-06日、03-13日、03-20日、04-03日、04-10日、04-17日予泰欣生200mg静滴6次。3、查体：腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肝区有轻叩痛，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。鉴别诊断：１、胆总管肿瘤：患者可有慢性腹痛，黄疸伴肝功能损害，B超常提示胆总管扩张，与患者不符，可排除。2、慢性胰腺炎：可有腹痛、腹泻、血糖升高等临床表现，B超、CT等提示胰腺钙化、胰管结石、胰腺假性囊肿等影像学表现。可查CA199、超声内镜＋FNA等排除。诊疗计划： 1、按消化科护理常规，一级护理，低脂半流。2、完善相关检查。3、请示上级医师决定进一步诊治方案。胡良嗥/刘明浩2011-06-10 16:46　　　　　胡良嗥主治医师首次查房记录今日胡良嗥主治医师查房：补充的病史和体征：无特殊病史补充。今日患者病情平稳，一般情况可。主治医师查房后指出：主治医师48小时诊断：胰腺癌。诊断依据：1、老年女，慢性起病，治疗史明确，病程较长。2、患者缘于2011年1月无明显诱因出现中上腹胀，进食后明显，无恶心、呕吐，皮肤巩膜逐渐黄染，全身瘙痒，伴有浓茶样小便，无发热、寒战。1月9日于我院急诊行腹部B超诊断为\"肝内外胆管扩张\"，1月10日收住我院，CT、MRCP、EUS均提示胰腺占位，考虑胰腺癌伴胆道系统扩张。FNA液基培养找到癌细胞。2011-1-20日行ERCP并置入一5cm、8.5F塑料支架和一7cm、10F的粒子支架，粒子共10颗。2011-03-06日、03-13日、03-20日\\04-03日、04-10日、04-17日予泰欣生200mg静滴6次。3、查体：腹平坦，无腹壁静脉曲张，腹部柔软，无压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肝区有轻叩痛，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。鉴别诊断：１、胆总管肿瘤：患者可有慢性腹痛，黄疸伴肝功能损害，B超常提示胆总管扩张，与患者不符，可排除。2、慢性胰腺炎：可有腹痛、腹泻、血糖升高等临床表现，B超、CT等提示胰腺钙化、胰管结石、胰腺假性囊肿等影像学表现。可查CA199、超声内镜＋FNA等排除。治疗计划： 1、按消化科护理常规，一级护理，低脂半流。2、完善胰腺CT及上消化道碘水造影等检查。孙畅2011-06-12 10:19　　　　　李兆申主诊医师首次查房记录今日李兆申教授查房后分析病情如下：补充的病史和体征：无特殊病史补充。今日患者病情平稳，一般情况可。CT：胰体癌并胰管扩张；肝左叶血管瘤可能大，肝右叶病灶考虑坏死；肝肾囊肿；胆囊结石，胆囊炎并肝内胆管积气扩张。诊断、病情、治疗方法分析讨论：患者胰腺癌诊断明确。注意事项：注意观察患者病情变化。孙畅2011-06-15 15:44　　　　　胡良嗥主治医师查房记录   今日胡良皞主治医师查房，患者未诉不适。今日患者病情平稳，一般情况可。查体情况:神清，心肺未见明显异常，腹平软，无压痛反跳痛。辅助检查结果：血总胆红素10.4umol/L、白蛋白30g/L、碱性磷酸酶312U/L、葡萄糖8.6mmol/L，胡良皞主治医师查房后指示：患者病情尚平稳，继续同前治疗。孙畅2011-06-18 08:55　　　　　李兆申主任医师查房记录　　今日李兆申主诊医师查房，患者述乏力。一般情况欠佳，精神一般，睡眠可，食欲减退，大小便无异常。查体情况:心肺未见异常，腹平软，无压痛反跳痛。辅助检查2011-6-16血常规：HCT36%，PLT90x10^9/L，李兆申主任医师查房后指示：患者体质虚弱，继续抗炎、营养支持等对症支持治疗，密切观察病情变化。孙畅2011-06-21 08:40　　　　　胡良嗥主治医师查房记录   今日胡良皞主治医师查房，患者乏力，一般情况欠佳，查体：神清，心肺未见明显异常，腹平软，无压痛反跳痛。胡良皞主治医师查房后指示：今日予以泰欣生治疗，继续营养支持，密切观察病情。孙畅2011-06-24 08:38　　　　　李兆申主任医师查房记录　　今日李兆申主诊医师查房，今日患者病情平稳，一般情况可。查体:神清，心肺未见明显异常，腹平软，无压痛反跳痛。李兆申主任医师查房后指示：患者病情平稳，继续营养支持治疗。孙畅2011-06-27 07:46　　　　　廖专主治医师查房记录   今日廖专主治医师查房，患者诉发热。今日患者病情平稳，一般情况可。查体情况:神清，心肺未见明显异常，腹平软，无压痛反跳痛。廖专主治医师查房后指示：患者今晨有发热，予以甲磺酸帕珠沙星、奥硝唑抗炎；吲哚美辛栓纳肛退热等对症支持治疗。孙畅2011-06-30 07:51　　　　　李兆申主诊医师查房记录　　今日李兆申主诊医师查房，患者未诉不适。今日患者病情平稳，一般情况可。查体情况:神清，心肺未见明显异常，腹平软，无压痛反跳痛。李兆申主任医师查房后指示：2011-06-28已予以泰欣生治疗，患者未诉不适，目前病情平稳，要求出院，予以同意。孙畅").replaceAll("\\\\", "\\\\\\\\\\\\").replaceAll("'","\\\\'"));
        System.out.println("test'''fadfa".replaceAll("'","\\\\'"));
    }

}