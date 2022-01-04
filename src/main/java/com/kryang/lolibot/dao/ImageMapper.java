package com.kryang.lolibot.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kryang.lolibot.pojo.ImageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper extends BaseMapper<ImageEntity> {
    Integer getMaxId();
}
