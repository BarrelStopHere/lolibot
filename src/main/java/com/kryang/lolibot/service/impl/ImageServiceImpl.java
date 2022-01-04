package com.kryang.lolibot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kryang.lolibot.dao.ImageMapper;
import com.kryang.lolibot.pojo.ImageEntity;
import com.kryang.lolibot.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, ImageEntity> implements ImageService {

    @Autowired
    ImageMapper imageMapper;

    @Override
    public Integer getMaxId() {
        return imageMapper.getMaxId();
    }
}
