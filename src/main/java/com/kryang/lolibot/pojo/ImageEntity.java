package com.kryang.lolibot.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ImageEntity implements Serializable {
    private final static long serialVersionUID = -4654565537628701302L;
    private int id;
    private String tag;
    private String image;
    private String group;
    private Date create;
}
