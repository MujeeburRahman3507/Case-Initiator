package com.CaseTest.dto;

import org.antlr.v4.runtime.misc.NotNull;

public class RequestDTO {


    private String processkey;


    private String myvariable1;

    private  String myvariable2;

    public String getProcesskey() {
        return processkey;
    }

    public void setProcesskey(String processkey) {
        this.processkey = processkey;
    }

    public String getMyvariable1() {
        return myvariable1;
    }

    public void setMyvariable1(String myvariable1) {
        this.myvariable1 = myvariable1;
    }

    public String getMyvariable2() {
        return myvariable2;
    }

    public void setMyvariable2(String myvariable2) {
        this.myvariable2 = myvariable2;
    }

    @Override
    public String toString() {
        return "RequestDTO{" +
                "processkey='" + processkey + '\'' +
                ", myvariable1='" + myvariable1 + '\'' +
                ", myvariable2='" + myvariable2 + '\'' +
                '}';
    }
}

