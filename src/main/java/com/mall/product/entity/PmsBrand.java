package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_brand")
public class PmsBrand {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String firstLetter;

    private Integer sort;

    private Integer factoryStatus;

    private Integer showStatus;

    private Integer productCount;

    private Integer productCommentCount;

    private String logo;

    private String bigPic;

    private String brandStory;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}