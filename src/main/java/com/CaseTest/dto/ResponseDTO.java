package com.CaseTest.dto;

public class ResponseDTO {
    private String wsstatuscode;
    private String wsmessage;
    private Object data;

    public ResponseDTO(String wsstatuscode, String wsmessage, Object data) {
        this.wsstatuscode = wsstatuscode;
        this.wsmessage = wsmessage;
        this.data = data;
    }

    public ResponseDTO() {

    }

    public String getWsstatuscode() {
        return wsstatuscode;
    }

    public void setWsstatuscode(String wsstatuscode) {
        this.wsstatuscode = wsstatuscode;
    }

    public String getWsmessage() {
        return wsmessage;
    }

    public void setWsmessage(String wsmessage) {
        this.wsmessage = wsmessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
