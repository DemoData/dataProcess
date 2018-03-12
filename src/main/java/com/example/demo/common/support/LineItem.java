package com.example.demo.common.support;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineItem {
    /**
     * 原文行内容
     */
    String line;
    /**
     * 行特征数据
     */
    JSONObject features;
    /**
     * 推荐锚点，该行需要推荐的锚点
     */
    String candidateAnchor = null;
    /**
     * 该行里的锚点信息
     */
    ArrayList<AnchorInfo> anchorInfos = new ArrayList<AnchorInfo>();
    boolean isFirstLine = false;


    private String dateFormatStrEn = "^(?!0000)[0-9]{4}-[\\s\\S]*";
    private String dateFormatStrZh = "^(?!0000)[0-9]{4}－[\\s\\S]*";

    protected Pattern patternEn = Pattern.compile(dateFormatStrEn);
    protected Pattern patternZh = Pattern.compile(dateFormatStrZh);

    /**
     * TODO 这里由用了一个正则是不对的，因为没有完整的日期正则，这里偷懒只用一个来截取位置
     */
    protected List<Pattern> dataPatternList= new ArrayList<>();
    private String dateFormatStrRecordDate = "^(?!0000)[0-9]{4}[ －　\\-:：0-9]+";

    protected Pattern patternRecordDate = Pattern.compile(dateFormatStrRecordDate);

    LineItem(String line, JSONObject features, boolean isFirstLine) {
        this.line = line;
        this.features = features;
        this.isFirstLine = isFirstLine;
    }

    public void setAnchorInfos(ArrayList<AnchorInfo> anchorInfos) {
        this.anchorInfos = anchorInfos;
    }

    public ArrayList<AnchorInfo> getAnchorInfos() {
        return anchorInfos;
    }

    public String getLine() {
        return line;
    }

    public String getCandidateAnchor() {
        return candidateAnchor;
    }

    public String getLineWithAnchorBlackBracket(int lineCounter, JSONObject recordFeatures) {
        String lineWithA = line;
        if (anchorInfos.size() > 0) {
            StringBuffer sb = new StringBuffer();
            char lastChar = 0;
            char[] chars = line.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                for (int j = 0; j < anchorInfos.size(); j++) {
                    AnchorInfo anchorInfo = anchorInfos.get(j);
                    if (anchorInfo.startPos == i) {
                        sb.append('【');
                        if (lastChar != '【') {
                            //如果本身没有黑框，还需补充一个
                            sb.append('【');
                        }
                    }
                }

                //本身内容的字符
                sb.append(chars[i]);
                lastChar = chars[i];

//                if (line.endsWith("三、【诊断依据】")){
//                    System.out.println("ddd");
//                }
                for (int j = 0; j < anchorInfos.size(); j++) {
                    AnchorInfo anchorInfo = anchorInfos.get(j);
                    if (anchorInfo.endPos == i) {
                        sb.append('】');
                        if (i + 1 < chars.length && chars[i + 1] != '】' || i == line.length() - 1) {
                            //如果本身没有黑框，还需补充一个
                            sb.append('】');
                        }
                    }
                }
            }

            lineWithA = sb.toString();
            if(lineWithA.contains("【【【")){
                lineWithA = lineWithA.replaceAll("【【【","【【");
            }
            if(lineWithA.contains("】】】"))
            lineWithA = lineWithA.replaceAll("】】】","】】");
        }

        //第二行如果没有锚点，就给一个默认的
        int addNormalAtLine = 1;
        if (lineCounter == addNormalAtLine && !lineWithA.startsWith("【【")){
            lineWithA = "【【无段落标题】】" + lineWithA;
        }

        //打上记录时间
        if (lineCounter == 0){
            if ( (patternEn.matcher(lineWithA).find() || patternZh.matcher(lineWithA).find())){
                //TODO 这里由用了一个正则是不对的，因为没有完整的日期正则，这里偷懒只用一个来截取位置
                Matcher matcher = null;
                Pattern pattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}\\s*\\d{1,2}:\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}\\s*\\d{1,2}：\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}－\\d{1,2}－\\d{1,2}\\s*\\d{1,2}:\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}－\\d{1,2}－\\d{1,2}\\s*\\d{1,2}：\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}－\\d{1,2}－\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s*\\d{1,2}:\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s*\\d{1,2}：\\d{1,2}");
                dataPatternList.add(pattern);
                pattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");
                dataPatternList.add(pattern);
                for(int i =0; i< dataPatternList.size(); i++){
                    matcher = dataPatternList.get(i).matcher(lineWithA);
                    if (matcher.find()){
                        int startPos = matcher.start(0);
                        int endPos = matcher.end(0);
                        String datePart = lineWithA.substring(startPos, endPos);
                        String otherPart = lineWithA.length() > endPos ? lineWithA.substring(endPos, lineWithA.length()) : "";
                        lineWithA = i+"【【记录时间】】" + datePart + (otherPart.trim().length() > 0 ? ("【【原文记录标题】】" + otherPart) : "【【时间结束标记】】" );
                        break;
                    }
                }


            }
        }

        return lineWithA;
    }
}