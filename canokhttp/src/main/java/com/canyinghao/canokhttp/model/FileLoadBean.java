package com.canyinghao.canokhttp.model;

import com.canyinghao.canokhttp.annotation.DownloadStatus;
import com.canyinghao.canokhttp.callback.CanCallBack;

import java.io.Serializable;


public class FileLoadBean implements Serializable {

    //  是否是上传
    public boolean isUpLoad;

    //文件上传下载接口地址
    public String url;

    //    上传参数名
    public String fileParam;
    //    上传文件路径
    public String filePath;
    //文件保存目录
    public String saveFileDir;
    //文件保存名称
    public String saveFileName;


    public String saveFileNameWithExtension;//保存文件名称：包含扩展名
    public String saveFileNameCopy;//保存文件备用名称：用于文件名称冲突
    public String saveFileNameEncrypt;//保存文件名称（加密后）


    public FileLoadBean(String url, String saveFileDir, String saveFileName) {
        this.url = url;
        this.saveFileDir = saveFileDir;
        this.saveFileName = saveFileName;
    }


    public FileLoadBean(String url, String fileParam, String filePath, boolean isUpLoad) {
        this.url = url;
        this.fileParam = fileParam;
        this.filePath = filePath;
        this.isUpLoad = isUpLoad;
    }

    public FileLoadBean() {
    }
}
