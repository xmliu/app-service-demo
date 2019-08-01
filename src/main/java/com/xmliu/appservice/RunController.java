package com.xmliu.appservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xmliu.appservice.entity.LoginBean;
import com.xmliu.appservice.entity.MobileResult;
import com.xmliu.appservice.entity.ResultBean;
import com.xmliu.appservice.mapper.LoginMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

import static java.lang.System.out;

/**
 * Created by diyangxia on 2017/3/6.
 */
@org.springframework.stereotype.Controller
public class RunController {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;


    /**
     * 获取数据库中的列表，get方式
     *
     * @return
     */
    @RequestMapping(value = "userList", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> userList() {
        out.println("welcome to userList");
        Map<String, Object> map = new HashMap<>();
        ResultBean result = onUserList();
        out.println("result==>" + result);
        map.put("code", result.getCode());
        map.put("reason", result.getReason());
        map.put("success", result.isSuccess());
        map.put("records", result.getRecords());
        return map;
    }

    /**
     * 用户登陆，Post方式
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> login(@RequestParam("param") String param) {
        out.println("welcome to login on post,param=" + param);
        ObjectMapper objectMapper = new ObjectMapper(); //转换器
        Map<String, Object> map = new HashMap<>();
        try {
            LoginBean loginBean = objectMapper.readValue(param, LoginBean.class); //json转换成map
            ResultBean result = onLogin(loginBean.getName(), loginBean.getPassword());
            out.println("result==>" + result);
            map.put("code", result.getCode());
            map.put("reason", result.getReason());
            map.put("success", result.isSuccess());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 用户注册，get方式
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "register", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> register(@RequestParam("username") String username, @RequestParam("password") String password) {
        out.println("welcome to register,username=" + username + ";password=" + password);
        Map<String, Object> map = new HashMap<>();
        ResultBean result = onRegister(username, password);
        out.println("result==>" + result);

        map.put("code", result.getCode());
        map.put("reason", result.getReason());
        map.put("success", result.isSuccess());
        return map;
    }


    private ResultBean onRegister(String username, String password) {
        ResultBean resultBean = new ResultBean();
        SqlSession session = null;
        try {
            session = sqlSessionFactory.openSession();
            LoginMapper loginMapper = session.getMapper(LoginMapper.class);
            Map<String, Object> map = new HashMap<>();
            map.put("name", username);
            map.put("password", password);
            LoginBean bean = new LoginBean();
            bean.setName(username);
            bean.setPassword(password);
            // 查询用户是否存在
            LoginBean userExist = loginMapper.findUserByName(map);
            if (userExist != null) {
                // 存在后无法注册
                resultBean.setCode("001");
                resultBean.setSuccess(false);
                resultBean.setReason("用户已存在");
            } else {
                loginMapper.addUser(bean);
                session.commit();
                System.out.println("当前增加的用户id为：" + bean.getId());
                resultBean.setCode("200");
                resultBean.setSuccess(true);
                resultBean.setReason("注册成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("注册异常==>" + e.getMessage());
            resultBean.setCode("001");
            resultBean.setSuccess(false);
            resultBean.setReason("注册异常");
        } finally {
            session.close();
        }
        return resultBean;
    }

    private ResultBean onUserList() {

        ResultBean resultBean = new ResultBean();
        SqlSession session = null;
        try {
            session = sqlSessionFactory.openSession();
            LoginMapper loginMapper = session.getMapper(LoginMapper.class);
            List<LoginBean> data = loginMapper.loadUserList();
            if (data.size() != 0) {
                ObjectMapper mapper = new ObjectMapper();
                resultBean.setCode("200");
                resultBean.setSuccess(true);
                resultBean.setRecords(mapper.writeValueAsString(data));
                resultBean.setReason("查询成功" + data.size());
            } else {
                resultBean.setCode("001");
                resultBean.setSuccess(false);
                resultBean.setReason("查询错误");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("查询异常==>" + e.getMessage());
            resultBean.setCode("001");
            resultBean.setSuccess(false);
            resultBean.setReason("查询异常");
        } finally {
            session.close();
        }
        return resultBean;
    }

    private ResultBean onLogin(String username, String password) {

        ResultBean resultBean = new ResultBean();
        SqlSession session = null;
        try {
            session = sqlSessionFactory.openSession();
            LoginMapper loginMapper = session.getMapper(LoginMapper.class);
            Map<String, Object> map = new HashMap<>();
            map.put("name", username);
            map.put("password", password);
            // 查询用户是否存在
            LoginBean userExist = loginMapper.findUserByName(map);
            if (userExist != null) {
                LoginBean data = loginMapper.loginUser(map);
                if (data != null) {
                    resultBean.setCode("200");
                    resultBean.setSuccess(true);
                    resultBean.setReason("登陆成功");
                } else {
                    resultBean.setCode("001");
                    resultBean.setSuccess(false);
                    resultBean.setReason("密码错误");
                }
            } else {
                resultBean.setCode("001");
                resultBean.setSuccess(false);
                resultBean.setReason("用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("登陆异常==>" + e.getMessage());
            resultBean.setCode("001");
            resultBean.setSuccess(false);
            resultBean.setReason("登陆异常");
        } finally {
            session.close();
        }
        return resultBean;
    }

    /**
     * 手机端上传文件
     * https://blog.csdn.net/kagami1983/article/details/46549941
     */
    @Resource
    private MobileResult mobileResult;

    @RequestMapping("/mobile/uploadfile")
    @ResponseBody
    public MobileResult uploadPhone(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request)
            throws IllegalStateException, IOException {

        String path = uploadFile(file, request);

        mobileResult.setCode("200");

        mobileResult.setPath(path);

        mobileResult.setMessage("上传成功");

        return mobileResult;

    }

    private String uploadFile(MultipartFile file, HttpServletRequest request) throws IOException {

//        String path = request.getSession().getServletContext().getRealPath("upload");
        String path = "/Users/liuxunming/Documents/AppService/Images/";
        String fileName = file.getOriginalFilename();

        File targetFile = new File(path, fileName);

        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }

        file.transferTo(targetFile);

        return targetFile.getAbsolutePath();

    }


    /**
     * PC端上传文件
     * https://www.cnblogs.com/achengmu/p/9183957.html
     */
    @RequestMapping(value = "/upload/one", method = RequestMethod.GET)
    public String uploadOneFile() {
        return "index";
    }

    @RequestMapping(value = "/do", method = RequestMethod.POST)
    public String uploadDo(HttpServletRequest request, Model model, @RequestParam("file") MultipartFile[] files) {
        //获取目录/创建路径
//        String uploadRootPath = request.getServletContext().getRealPath("upload");
        String uploadRootPath = "/Users/liuxunming/Documents/AppService/Images/";
        //获取路径
        File uploadRootDir = new File(uploadRootPath);
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }

        //用来存放上传后的路径地址的变量
        List<File> uploadFiles = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];

            //原文件名
            String name = file.getOriginalFilename();
            System.out.print(name);


            if (name != null && name.length() > 0) {
                try {

                    //获取文件字节流
                    byte[] bytes = file.getBytes();

                    //新文件路径
                    File serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + name);

                    //将文件字节流输出到刚创建的文件上
                    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                    stream.write(bytes);
                    stream.close();

                    //将文件路径添加到uploadFiles中
                    uploadFiles.add(serverFile);
                    System.out.println(serverFile);


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                    System.out.println("error to file:" + name);
                }
            }

        }
        model.addAttribute("uploadFiles", uploadFiles);
        return "uploadResult";

    }

}
