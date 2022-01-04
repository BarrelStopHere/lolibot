package com.kryang.lolibot.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kryang.lolibot.pojo.ImageEntity;

public interface ImageService extends IService<ImageEntity> {

    /**
     * 返回最大id
     * @return
     */
    Integer getMaxId();

}
