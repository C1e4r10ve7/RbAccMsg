package com.inspur.gs.RbAccMsg.entity;

public class Wbstatus {
    private String PZID;
    private String STATUS;
    private String EXTEND01;
    private String EXTEND02;
    private String EXTEND03;

    public Wbstatus() {
    }

    public Wbstatus(String PZID, String STATUS, String EXTEND01, String EXTEND02, String EXTEND03) {
        this.PZID = PZID;
        this.STATUS = STATUS;
        this.EXTEND01 = EXTEND01;
        this.EXTEND02 = EXTEND02;
        this.EXTEND03 = EXTEND03;
    }

    public String getPZID() {
        return PZID;
    }

    public void setPZID(String PZID) {
        this.PZID = PZID;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getEXTEND01() {
        return EXTEND01;
    }

    public void setEXTEND01(String EXTEND01) {
        this.EXTEND01 = EXTEND01;
    }

    public String getEXTEND02() {
        return EXTEND02;
    }

    public void setEXTEND02(String EXTEND02) {
        this.EXTEND02 = EXTEND02;
    }

    public String getEXTEND03() {
        return EXTEND03;
    }

    public void setEXTEND03(String EXTEND03) {
        this.EXTEND03 = EXTEND03;
    }
}
