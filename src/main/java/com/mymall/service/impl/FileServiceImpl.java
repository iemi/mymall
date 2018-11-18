package com.mymall.service.impl;

import com.google.common.collect.Lists;
import com.mymall.service.IFileService;
import com.mymall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService{

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 文件上传
     * @param file
     * @param path
     * @return
     */
    public String upload(MultipartFile file,String path){

        //获取文件名
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String fileExName = fileName.substring(fileName.lastIndexOf(".") + 1);
        //为了避免文件名重复 导致覆盖
        String uploadName = UUID.randomUUID().toString() + "." + fileExName;

        logger.info("开始上传文件，上传文件名{}，上传路径{}，新文件名{}",fileName,path,uploadName);

        //创建文件路径
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path,uploadName);
        try {
            file.transferTo(targetFile);//文件上传成功 只是上传到服务器tomcat下
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));//上传到ftp服务器上
            targetFile.delete();//删除upload下面的文件
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }

        return targetFile.getName();
    }

}
