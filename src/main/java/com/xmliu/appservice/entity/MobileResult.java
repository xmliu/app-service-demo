package com.xmliu.appservice.entity;

import org.springframework.stereotype.Component;

/**
 * Author : liuxunming
 * Date : 2019/7/31
 * TIME : 22:48
 * TODO
 */
@Component
public class MobileResult {

    private String path;
    private String code;
    private String message;
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

}
