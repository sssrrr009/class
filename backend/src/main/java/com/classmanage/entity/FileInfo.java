package com.classmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * FileInfo 实体类
 */
@Data
@TableName("file_info")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 文件名 */
    private String fileName;

    /** 分类 */
    private Long categoryId;

    /** 说明 */
    private String description;

    /** 路径 */
    private String filePath;

    /** 大小 */
    private Long fileSize;

    /** 上传人 */
    private String uploader;

    /** 上传时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}