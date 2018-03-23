package com.example.demo.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.corpus.Parser;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

public class TextAnalyzer {

    static final double CHUNK_SCORE = 1.0;
    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new IndoEuropeanSentenceModel();

    public static void main(String[] args) {
    	testChunkSentences();
    	testChunkDictionary();
        test();
    }

    private static void test() {
    }

    // Sentences - Sentences Chunking（分句）
    private static void testChunkSentences() {
        String text = "入 院 记 录 姓名： 施锡明 性别： 男 年龄： 77岁 民族： 汉 籍贯： 上海市 婚姻： 已婚 职业： 务农 供史者： 患者本人 现居住地址： 上海市崇明县建设镇滧东6队 入院时间： 2011-02-23 12:37 记录时间： 2011-02-23 15:42 主 诉： 右下肢 静息痛 一月余。 现病史： 患者于2011年1月 逐渐出现 右 下肢 静息痛 ，夜间加重。于上海市崇明中心医院查血管彩超示右下肢股动脉狭窄。服用盐酸沙格雷酯等药物保守治疗，效果欠佳。 今为进一步治疗来我院就诊，门诊以\"下肢ASO\"收入院。自发病以来，病人 精神状态良好 ， 体力情况良好 ， 食欲食量良好 ， 睡眠情况良好 ， 体重无明显变化 ， 大便正常 ， 小便正常 。 仍需治疗的其他疾病情况 ： 无 。 既往史： 40年前因外伤右侧股骨头坏死，行保守治疗，效果可。有胆结石、胆囊炎病史4年，行消炎，利胆治疗，病情控制可。小脑梗塞病史4年，未予规律治疗，自服血塞通，病情控制可。 自述有高血压病史数年，血压最高为150/100，口服非洛地平，血压控制可，一般控制在130/80。否认“伤寒、结核、肝炎”等传染病史， 否认冠心病、糖尿病”史 ， 否认手术、外伤史 ， 否认输血史 、 否认食物、药物过敏史 ， 预防接种史不详 。 个人史： 生于 上海市 ，久居 本地 ， 无疫源接触史 ， 无粉尘及有毒化学物品、放射性物质接触史 ， 吸烟 40 年 ，平均 7 支／日， 未戒烟 。吸烟指数280年支 ， 无冶游史 。";
        List<String> result = new ArrayList<String>();

        List<String> tokenList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(),
                0, text.length());
        tokenizer.tokenize(tokenList, whiteList);
        String[] tokens = new String[tokenList.size()];
        String[] whites = new String[whiteList.size()];
        tokenList.toArray(tokens);
        whiteList.toArray(whites);
        int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens,
                whites);
        int sentStartTok = 0;
        int sentEndTok = 0;
        for (int i = 0; i < sentenceBoundaries.length; ++i) {
            System.out.println("Sentense " + (i + 1) + ", sentense's length(from 0):" + (sentenceBoundaries[i]));
            StringBuilder sb = new StringBuilder();
            sentEndTok = sentenceBoundaries[i];
            for (int j = sentStartTok; j <= sentEndTok; j++) {
                sb.append(tokens[j]).append(whites[j + 1]);
            }
            sentStartTok = sentEndTok + 1;
            result.add(sb.toString());
        }
        System.out.println("Final result:" + result);
    }

    // NER(named entity recognition) - Exact Dictionary-Based Chunking（分词）
    private static void testChunkDictionary() {
        String[] args1 = {"入 院 记 录 姓名： 施锡明 性别： 男 年龄： 77岁 民族： 汉 籍贯： 上海市 婚姻： 已婚 职业： 务农 供史者： 患者本人 现居住地址： 上海市崇明县建设镇滧东6队 入院时间： 2011-02-23 12:37 记录时间： 2011-02-23 15:42 主 诉： 右下肢 静息痛 一月余。 现病史： 患者于2011年1月 逐渐出现 右 下肢 静息痛 ，夜间加重。于上海市崇明中心医院查血管彩超示右下肢股动脉狭窄。服用盐酸沙格雷酯等药物保守治疗，效果欠佳。 今为进一步治疗来我院就诊，门诊以\"下肢ASO\"收入院。自发病以来，病人 精神状态良好 ， 体力情况良好 ， 食欲食量良好 ， 睡眠情况良好 ， 体重无明显变化 ， 大便正常 ， 小便正常 。 仍需治疗的其他疾病情况 ： 无 。 既往史： 40年前因外伤右侧股骨头坏死，行保守治疗，效果可。有胆结石、胆囊炎病史4年，行消炎，利胆治疗，病情控制可。小脑梗塞病史4年，未予规律治疗，自服血塞通，病情控制可。 自述有高血压病史数年，血压最高为150/100，口服非洛地平，血压控制可，一般控制在130/80。否认“伤寒、结核、肝炎”等传染病史， 否认冠心病、糖尿病”史 ， 否认手术、外伤史 ， 否认输血史 、 否认食物、药物过敏史 ， 预防接种史不详 。 个人史： 生于 上海市 ，久居 本地 ， 无疫源接触史 ， 无粉尘及有毒化学物品、放射性物质接触史 ， 吸烟 40 年 ，平均 7 支／日， 未戒烟 。吸烟指数280年支 ， 无冶游史 。"};

        MapDictionary<String> dictionary = new MapDictionary<String>();
        dictionary.addEntry(new DictionaryEntry<String>("50 Cent","PERSON",CHUNK_SCORE));
        dictionary.addEntry(new DictionaryEntry<String>("XYZ120 DVD Player","DB_ID_1232",CHUNK_SCORE));
        dictionary.addEntry(new DictionaryEntry<String>("cent","MONETARY_UNIT",CHUNK_SCORE));
        dictionary.addEntry(new DictionaryEntry<String>("dvd player","PRODUCT",CHUNK_SCORE));


        ExactDictionaryChunker dictionaryChunkerTT
                = new ExactDictionaryChunker(dictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                true,true);

        ExactDictionaryChunker dictionaryChunkerTF
                = new ExactDictionaryChunker(dictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                true,false);

        // returnAllMatches is false means bypassing the matched text from further matching process
        ExactDictionaryChunker dictionaryChunkerFT
                = new ExactDictionaryChunker(dictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                false,true);

        ExactDictionaryChunker dictionaryChunkerFF
                = new ExactDictionaryChunker(dictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                false,false);



        System.out.println("\nDICTIONARY\n" + dictionary);

        for (int i = 0; i < args1.length; ++i) {
            String text = args1[i];
            System.out.println("\n\nTEXT=" + text);

            chunk(dictionaryChunkerTT,text);
            chunk(dictionaryChunkerTF,text);
            chunk(dictionaryChunkerFT,text);
            chunk(dictionaryChunkerFF,text);
        }
    }

    static void chunk(ExactDictionaryChunker chunker, String text) {
        System.out.println("\nChunker."
                + " All matches=" + chunker.returnAllMatches()
                + " Case sensitive=" + chunker.caseSensitive());
        Chunking chunking = chunker.chunk(text);
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String type = chunk.type();
            double score = chunk.score();
            String phrase = text.substring(start,end);
            System.out.println("     phrase=|" + phrase + "|"
                    + " start=" + start
                    + " end=" + end
                    + " type=" + type
                    + " score=" + score);
        }
    }
}