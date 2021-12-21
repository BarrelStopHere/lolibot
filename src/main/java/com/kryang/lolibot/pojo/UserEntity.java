package com.kryang.lolibot.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserEntity implements Serializable {
    private final static long serialVersionUID = -4654565537628701302L;
    private Integer id;
    private Integer qq;//qq号
    private String head;//头像
    private String favorite;//图片收藏

}
