package com.kryang.lolibot.pojo;

import com.mysql.jdbc.Blob;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ImageEntity implements Serializable {
    private final static long serialVersionUID = -4654565537628701302L;
    private int id;
    private String name;
    private String tag;
    private Blob image;
    private String group;
    private Date create;
}
