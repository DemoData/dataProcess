package com.example.demo.common.support;

public class AnchorInfo {

    int startPos;
    int endPos;
    String anchor;

    private boolean isLegal = true;

    public AnchorInfo(int startPos, int endPos, String anchor){
        this.anchor = anchor;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public void checkLegal(AnchorInfo other){
        if (other == this){
            return;
        }

        //TODO 也许要参考优先级名单
        if (other.startPos < this.startPos && other.endPos >= this.endPos || other.startPos <= this.startPos && other.endPos > this.endPos){
            //被包含的无效
            isLegal = false;
        } else if (startPos < other.startPos && endPos > other.startPos && endPos < other.endPos){
            //交叉，取后者
            isLegal = false;
        } else if (endPos == other.startPos-1){
            //相邻，取后者
            isLegal = false;
        }
    }

    public boolean isLegal() {
        return isLegal;
    }

}
