package com.xmliu.appservice.mapper;

import com.xmliu.appservice.entity.LoginBean;

import java.util.List;
import java.util.Map;

/**
 * Created by diyangxia on 2017/3/10.
 */
public interface LoginMapper {

    public LoginBean findUserByName(Map<String,Object> map) throws Exception;
    public LoginBean loginUser(Map<String,Object> map) throws Exception;
    public void addUser(LoginBean bean) throws Exception;
    public List<LoginBean> loadUserList() throws Exception;

}
