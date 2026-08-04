import os

# (类名, 表名, [字段...])  字段: 名,类型映射
# Java类型: I=Integer, L=Long, S=String, BD=BigDecimal, D=LocalDateTime, TS=LocalDateTime

entities = {
"Admin": ("admin", [("id","L","主键"),("username","S","登录用户名"),("password","S","登录密码"),("realName","S","管理员姓名"),("createTime","TS","创建时间")]),
"College": ("college", [("id","L","主键"),("collegeName","S","学院名称"),("createTime","TS","创建时间")]),
"Major": ("major", [("id","L","主键"),("collegeId","L","所属学院"),("majorName","S","专业名称"),("majorCode","S","专业编号"),("createTime","TS","创建时间")]),
"Teacher": ("teacher", [("id","L","主键"),("teacherNo","S","工号"),("password","S","密码"),("name","S","姓名"),("gender","S","性别"),("phone","S","电话"),("collegeId","L","所属学院"),("title","S","职称"),("createTime","TS","创建时间")]),
"ClassInfo": ("class_info", [("id","L","主键"),("classNo","S","班级号"),("className","S","班级名称"),("majorId","L","所属专业"),("teacherId","L","班主任"),("studentCount","I","班级人数"),("createTime","TS","创建时间")]),
"Student": ("student", [("id","L","主键"),("studentNo","S","学号"),("password","S","密码"),("name","S","姓名"),("gender","S","性别"),("phone","S","电话"),("idCard","S","身份证"),("classId","L","所属班级"),("createTime","TS","创建时间")]),
"Course": ("course", [("id","L","主键"),("courseCode","S","课程编号"),("courseName","S","课程名称"),("courseHours","I","课时"),("credit","BD","学分"),("teacherId","L","任课教师"),("classTime","S","上课时间"),("location","S","上课地点"),("status","S","课程状态"),("createTime","TS","创建时间")]),
"MajorPlan": ("major_plan", [("id","L","主键"),("majorId","L","专业"),("courseId","L","课程"),("yearLevel","I","学年"),("semester","I","学期"),("createTime","TS","创建时间")]),
"StudentCourse": ("student_course", [("id","L","主键"),("studentId","L","学生"),("courseId","L","课程"),("selectTime","TS","选课时间")]),
"Score": ("score", [("id","L","主键"),("studentId","L","学生"),("courseId","L","课程"),("score","BD","分数"),("gradeLevel","S","等级"),("createTime","TS","创建时间"),("updateTime","TS","更新时间")]),
"Notice": ("notice", [("id","L","主键"),("title","S","标题"),("content","S","内容"),("scope","S","范围"),("targetCollegeId","L","目标学院"),("targetMajorId","L","目标专业"),("targetClassId","L","目标班级"),("targetStudentId","L","目标学生"),("publisherRole","S","发布人角色"),("publisherName","S","发布人姓名"),("attachmentPath","S","附件路径"),("attachmentName","S","附件名"),("createTime","TS","发布时间")]),
"FileCategory": ("file_category", [("id","L","主键"),("categoryName","S","分类名称"),("createTime","TS","创建时间")]),
"FileInfo": ("file_info", [("id","L","主键"),("fileName","S","文件名"),("categoryId","L","分类"),("description","S","说明"),("filePath","S","路径"),("fileSize","L","大小"),("uploader","S","上传人"),("createTime","TS","上传时间")]),
"Vote": ("vote", [("id","L","主键"),("title","S","主题"),("content","S","内容"),("attachmentPath","S","附件路径"),("attachmentName","S","附件名"),("creatorRole","S","创建人角色"),("creatorName","S","创建人姓名"),("teacherId","L","教师"),("cadreId","L","干部"),("maxVotes","I","每人最大票数"),("status","I","状态"),("createTime","TS","创建时间")]),
"VoteOption": ("vote_option", [("id","L","主键"),("voteId","L","活动"),("optionText","S","选项内容")]),
"Cadre": ("cadre", [("id","L","主键"),("studentId","L","学生"),("cadreType","S","干部类型"),("classId","L","班级"),("unionScope","S","学生会范围"),("createTime","TS","创建时间")]),
"StudentVote": ("student_vote", [("id","L","主键"),("voteId","L","活动"),("studentId","L","学生"),("optionId","L","选项"),("voteTime","TS","投票时间")]),
}

TYPE_MAP = {
    "L": ("Long", "Long"),
    "I": ("Integer", "Integer"),
    "S": ("String", "String"),
    "BD": ("BigDecimal", "java.math.BigDecimal"),
    "TS": ("LocalDateTime", "java.time.LocalDateTime"),
}

for cls, (table, fields) in entities.items():
    imports = {"com.baomidou.mybatisplus.annotation.TableId",
               "com.baomidou.mybatisplus.annotation.TableName",
               "com.baomidou.mybatisplus.annotation.IdType",
               "lombok.Data",
               "java.io.Serializable"}
    # 需要填充(时间戳)的字段自动填充
    fill_fields = [f for f in fields if f[1] == "TS"]
    if fill_fields:
        imports.add("com.baomidou.mybatisplus.annotation.FieldFill")
        imports.add("com.baomidou.mybatisplus.annotation.TableField")

    lines = []
    lines.append("package com.classmanage.entity;")
    lines.append("")
    for imp in sorted(imports):
        lines.append(f"import {imp};")
    lines.append("")
    lines.append("/**")
    lines.append(f" * {cls} 实体类")
    lines.append(" */")
    lines.append("@Data")
    lines.append(f'@TableName("{table}")')
    lines.append("public class " + cls + " implements Serializable {")
    lines.append("")
    lines.append("    private static final long serialVersionUID = 1L;")
    lines.append("")
    for name, t, comment in fields:
        jt, _ = TYPE_MAP[t]
        if name == "id":
            lines.append(f'    /** {comment} */')
            lines.append('    @TableId(value = "id", type = IdType.AUTO)')
            lines.append(f"    private {jt} id;")
        else:
            if t == "TS":
                lines.append(f'    /** {comment} */')
                lines.append(f'    @TableField(fill = FieldFill.INSERT)')
                lines.append(f"    private {jt} {name};")
            else:
                lines.append(f'    /** {comment} */')
                lines.append(f"    private {jt} {name};")
        lines.append("")
    lines.append("}")
    with open(f"{cls}.java", "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

print("生成实体类:", len(entities))
