package com.kryang.lolibot.pojo;

import com.mysql.jdbc.Blob;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ImageEntity implements Serializable {
    private final static long serialVersionUID = -4654565537628701302L;
    private Integer id;
    private String name;
    private String tag;
    private String url;
    private String group;
    private Date create;
    private Date update;
}
