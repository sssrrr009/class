-- ============================================================
-- 智慧班级管理系统  数据库初始化脚本（V2.0）
-- 数据库：class_management   MySQL 8.0+  utf8mb4
-- 用法：mysql -uroot -p < init.sql
--
-- 登录账号说明：
--   管理员 admin / admin123
--   教师   T001 王老师 / 123456
--   学生  2023001 张三 / 011234（身份证后6位）
--   学生  2023002 李四 / 022345
--   学生  2023003 王五 / 033456
--   干部  复用学生账号，选择"干部"角色登录：
--         张三(班长)、李四(学生会-校级)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `class_management`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `class_management`;

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 管理员表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
  `password`    VARCHAR(100) NOT NULL COMMENT '登录密码(BCrypt加密)',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '管理员姓名',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ------------------------------------------------------------
-- 2. 学院表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `college`;
CREATE TABLE `college` (
  `id`           INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `college_name` VARCHAR(100) NOT NULL COMMENT '学院名称',
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_college_name` (`college_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学院表';

-- ------------------------------------------------------------
-- 3. 专业表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `college_id`  INT         NOT NULL COMMENT '所属学院,关联college.id',
  `major_name`  VARCHAR(100) NOT NULL COMMENT '专业名称',
  `major_code`  VARCHAR(20) DEFAULT NULL COMMENT '专业编号',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_major_name` (`major_name`),
  KEY `idx_major_college` (`college_id`),
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业表';

-- ------------------------------------------------------------
-- 4. 教师(教职工)表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_no`  VARCHAR(20)  NOT NULL COMMENT '工号(登录用户名)',
  `password`    VARCHAR(100) NOT NULL COMMENT '登录密码(BCrypt加密)',
  `name`        VARCHAR(50)  NOT NULL COMMENT '姓名',
  `gender`      VARCHAR(10)  DEFAULT NULL COMMENT '性别',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `college_id`  INT          DEFAULT NULL COMMENT '所属学院,关联college.id',
  `title`       VARCHAR(50)  DEFAULT NULL COMMENT '职称(助教/讲师/副教授/教授)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  KEY `idx_teacher_college` (`college_id`),
  CONSTRAINT `fk_teacher_college` FOREIGN KEY (`college_id`) REFERENCES `college` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师(教职工)表';

-- ------------------------------------------------------------
-- 5. 班级表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `class_info`;
CREATE TABLE `class_info` (
  `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `class_no`      VARCHAR(20)  NOT NULL COMMENT '班级号',
  `class_name`    VARCHAR(50)  NOT NULL COMMENT '班级名称',
  `major_id`      INT          DEFAULT NULL COMMENT '所属专业,关联major.id',
  `teacher_id`    INT          DEFAULT NULL COMMENT '班主任,关联teacher.id',
  `student_count` INT          NOT NULL DEFAULT 0 COMMENT '班级人数',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_no` (`class_no`),
  KEY `idx_class_major` (`major_id`),
  KEY `idx_class_teacher` (`teacher_id`),
  CONSTRAINT `fk_class_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_class_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- ------------------------------------------------------------
-- 6. 学生表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_no`  VARCHAR(20)  NOT NULL COMMENT '学号(登录用户名)',
  `password`    VARCHAR(100) NOT NULL COMMENT '登录密码(BCrypt加密,初始为身份证后6位)',
  `name`        VARCHAR(50)  NOT NULL COMMENT '姓名',
  `gender`      VARCHAR(10)  DEFAULT NULL COMMENT '性别',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `id_card`     VARCHAR(18)  DEFAULT NULL COMMENT '身份证号码',
  `class_id`    INT          DEFAULT NULL COMMENT '所属班级,关联class_info.id',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  UNIQUE KEY `uk_student_idcard` (`id_card`),
  KEY `idx_student_class` (`class_id`),
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- ------------------------------------------------------------
-- 7. 课程类别表(新增)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category` (
  `id`            INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_code` VARCHAR(20) NOT NULL COMMENT '类别编号(如080109=大学英语二)',
  `category_name` VARCHAR(100) NOT NULL COMMENT '类别名称',
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cat_code` (`category_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程类别表';

-- ------------------------------------------------------------
-- 8. 选课配置表(新增)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `enroll_config`;
CREATE TABLE `enroll_config` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `is_open`     TINYINT  NOT NULL DEFAULT 0 COMMENT '是否开放选课 0关闭 1开放',
  `start_time`  DATETIME DEFAULT NULL COMMENT '选课开始时间',
  `end_time`    DATETIME DEFAULT NULL COMMENT '选课结束时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课配置表';

-- ------------------------------------------------------------
-- 9. 课程表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id`           INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_code`  VARCHAR(20)   NOT NULL COMMENT '课程编号',
  `course_name`  VARCHAR(100)  NOT NULL COMMENT '课程名称',
  `category_id`  INT           DEFAULT NULL COMMENT '课程类别,关联course_category.id',
  `course_hours` INT           DEFAULT NULL COMMENT '课时',
  `credit`       DECIMAL(3,1)  DEFAULT NULL COMMENT '学分',
  `capacity`     INT           DEFAULT 50 COMMENT '容纳人数',
  `teacher_id`   INT           DEFAULT NULL COMMENT '任课教师,关联teacher.id',
  `class_time`   VARCHAR(100)  DEFAULT NULL COMMENT '上课时间',
  `location`     VARCHAR(100)  DEFAULT NULL COMMENT '上课地点',
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'NOT_STARTED' COMMENT '课程状态:NOT_STARTED未开课/OPENING开课中/FINISHED已结课',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_code` (`course_code`),
  KEY `idx_course_teacher` (`teacher_id`),
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ------------------------------------------------------------
-- 8. 专业计划表(专业选课计划)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `major_plan`;
CREATE TABLE `major_plan` (
  `id`          INT     NOT NULL AUTO_INCREMENT COMMENT '主键',
  `major_id`    INT     NOT NULL COMMENT '专业,关联major.id',
  `category_id` INT     NOT NULL COMMENT '课程类别,关联course_category.id',
  `year_level`  INT     DEFAULT 1 COMMENT '第几学年(如1表示第一学年课程)',
  `semester`    INT     DEFAULT NULL COMMENT '学期(1/2,可空表示不限)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_major_category` (`major_id`, `category_id`),
  KEY `idx_plan_category` (`category_id`),
  CONSTRAINT `fk_plan_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_plan_category` FOREIGN KEY (`category_id`) REFERENCES `course_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业计划表';

-- ------------------------------------------------------------
-- 9. 学生选课表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `student_course`;
CREATE TABLE `student_course` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  INT      NOT NULL COMMENT '学生,关联student.id',
  `course_id`   INT      NOT NULL COMMENT '课程,关联course.id',
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '0候选 1已选中 2未选中',
  `select_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sc_student_course` (`student_id`, `course_id`),
  KEY `idx_sc_course` (`course_id`),
  CONSTRAINT `fk_sc_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sc_course`  FOREIGN KEY (`course_id`)  REFERENCES `course` (`id`)  ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生选课表';

-- ------------------------------------------------------------
-- 10. 成绩表(基于选课)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
  `id`          INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  INT           NOT NULL COMMENT '学生,关联student.id',
  `course_id`   INT           NOT NULL COMMENT '课程,关联course.id',
  `score`       DECIMAL(5,2)  DEFAULT NULL COMMENT '课程分数',
  `grade_level` VARCHAR(20)   DEFAULT NULL COMMENT '等级:优秀/良好/及格/不及格',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_score_student_course` (`student_id`, `course_id`),
  KEY `idx_score_course` (`course_id`),
  CONSTRAINT `fk_score_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_score_course`  FOREIGN KEY (`course_id`)  REFERENCES `course` (`id`)  ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- ------------------------------------------------------------
-- 11. 公告表(分级:学校/学院/专业/班级/个人)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`               INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`            VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content`          TEXT         COMMENT '公告内容',
  `scope`            VARCHAR(20)  NOT NULL DEFAULT 'CLASS' COMMENT '发布范围:SCHOOL学校/COLLEGE学院/MAJOR专业/CLASS班级/PERSONAL个人',
  `target_college_id` INT         DEFAULT NULL COMMENT '范围=学院时的目标学院,关联college.id',
  `target_major_id`   INT         DEFAULT NULL COMMENT '范围=专业时的目标专业,关联major.id',
  `target_class_id`   INT         DEFAULT NULL COMMENT '范围=班级时的目标班级,关联class_info.id',
  `target_student_id` INT         DEFAULT NULL COMMENT '范围=个人时的目标学生,关联student.id',
  `publisher_role`   VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '发布人角色:ADMIN管理员/TEACHER教师/CADRE干部',
  `publisher_name`   VARCHAR(50)  DEFAULT NULL COMMENT '发布人姓名(冗余,便于展示)',
  `attachment_path`  VARCHAR(255) DEFAULT NULL COMMENT '附件存储路径',
  `attachment_name`  VARCHAR(255) DEFAULT NULL COMMENT '附件原始文件名',
  `publish_time`     DATETIME     DEFAULT NULL COMMENT '定时发布时间',
  `target_student_ids` VARCHAR(1000) DEFAULT NULL COMMENT '个人公告目标学生ID(逗号分隔,支持多选)',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_notice_scope` (`scope`),
  KEY `idx_notice_target_class` (`target_class_id`),
  KEY `idx_notice_target_student` (`target_student_id`),
  KEY `idx_notice_publisher` (`publisher_role`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ------------------------------------------------------------
-- 12. 文件分类表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `file_category`;
CREATE TABLE `file_category` (
  `id`            INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称(文本/压缩/Word等)',
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件分类表';

-- ------------------------------------------------------------
-- 13. 文件信息表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id`           INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_name`    VARCHAR(255)  NOT NULL COMMENT '文件名',
  `category_id`  INT           DEFAULT NULL COMMENT '所属分类,关联file_category.id',
  `description`  VARCHAR(255)  DEFAULT NULL COMMENT '文件说明',
  `file_path`    VARCHAR(255)  NOT NULL COMMENT '存储路径',
  `file_size`    BIGINT        NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `uploader`     VARCHAR(50)   DEFAULT NULL COMMENT '上传人',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_category` (`category_id`),
  CONSTRAINT `fk_file_category` FOREIGN KEY (`category_id`) REFERENCES `file_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- ------------------------------------------------------------
-- 16. 学生干部表(复用学生账号)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `cadre`;
CREATE TABLE `cadre` (
  `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  INT         NOT NULL COMMENT '关联学生,复用学生账号,关联student.id',
  `cadre_type`  VARCHAR(20) NOT NULL COMMENT '干部类型:MONITOR班长/UNION学生会',
  `class_id`    INT         DEFAULT NULL COMMENT '班长所属班级,关联class_info.id,可空',
  `union_scope` VARCHAR(20) DEFAULT NULL COMMENT '学生会权限范围:SCHOOL/COLLEGE/MAJOR,高级可向下发布',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_cadre_student` (`student_id`),
  KEY `idx_cadre_class` (`class_id`),
  CONSTRAINT `fk_cadre_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cadre_class`  FOREIGN KEY (`class_id`)   REFERENCES `class_info` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生干部表';

-- ------------------------------------------------------------
-- 14. 投票活动表(可关联教师/干部,支持每人多票)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `vote`;
CREATE TABLE `vote` (
  `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`           VARCHAR(100) NOT NULL COMMENT '投票主题',
  `content`         TEXT         COMMENT '投票内容说明',
  `attachment_path` VARCHAR(255) DEFAULT NULL COMMENT '附件路径',
  `attachment_name` VARCHAR(255) DEFAULT NULL COMMENT '附件原始文件名',
  `creator_role`    VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '创建人角色:ADMIN管理员/TEACHER教师/CADRE干部',
  `creator_name`    VARCHAR(50)  DEFAULT NULL COMMENT '创建人姓名(冗余)',
  `teacher_id`      INT          DEFAULT NULL COMMENT '关联教师,关联teacher.id,可空',
  `cadre_id`        INT          DEFAULT NULL COMMENT '关联干部,关联cadre.id,可空',
  `max_votes`       INT          NOT NULL DEFAULT 1 COMMENT '每人最多投票数(支持多票)',
  `show_result`     TINYINT      NOT NULL DEFAULT 1 COMMENT '投票结果是否公开 1公开 0不公开',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1进行中,0已结束',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_vote_teacher` (`teacher_id`),
  KEY `idx_vote_cadre` (`cadre_id`),
  CONSTRAINT `fk_vote_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_vote_cadre` FOREIGN KEY (`cadre_id`) REFERENCES `cadre` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票活动表';

-- ------------------------------------------------------------
-- 15. 投票选项表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `vote_option`;
CREATE TABLE `vote_option` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vote_id`     INT          NOT NULL COMMENT '所属活动,关联vote.id',
  `option_text` VARCHAR(200) NOT NULL COMMENT '选项内容',
  PRIMARY KEY (`id`),
  KEY `idx_option_vote` (`vote_id`),
  CONSTRAINT `fk_option_vote` FOREIGN KEY (`vote_id`) REFERENCES `vote` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票选项表';

-- ------------------------------------------------------------
-- 17. 课程修改申请表(新增)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `course_change_request`;
CREATE TABLE `course_change_request` (
  `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id`     INT          NOT NULL COMMENT '课程,关联course.id',
  `teacher_id`    INT          NOT NULL COMMENT '申请教师,关联teacher.id',
  `field_name`    VARCHAR(50)  DEFAULT NULL COMMENT '修改字段(兼容单字段模式)',
  `old_value`     VARCHAR(255) DEFAULT NULL COMMENT '原值',
  `new_value`     VARCHAR(255) DEFAULT NULL COMMENT '新值',
  `new_data`      TEXT         COMMENT '修改后完整课程JSON(快照式)',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审批 1已通过 2已拒绝',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `handle_time`   DATETIME     DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_ccr_course` (`course_id`),
  KEY `idx_ccr_teacher` (`teacher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程修改申请表';

-- ------------------------------------------------------------
-- 18. 学生投票记录表(每人可对多选项投票,总票数由vote.max_votes控制)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `student_vote`;
CREATE TABLE `student_vote` (
  `id`         INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vote_id`    INT      NOT NULL COMMENT '投票活动,关联vote.id',
  `student_id` INT      NOT NULL COMMENT '投票学生,关联student.id',
  `option_id`  INT      NOT NULL COMMENT '所选选项,关联vote_option.id',
  `vote_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sv_vote_student_option` (`vote_id`, `student_id`, `option_id`),
  KEY `idx_sv_student` (`student_id`),
  CONSTRAINT `fk_sv_vote`    FOREIGN KEY (`vote_id`)    REFERENCES `vote` (`id`)        ON DELETE CASCADE,
  CONSTRAINT `fk_sv_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)     ON DELETE CASCADE,
  CONSTRAINT `fk_sv_option`  FOREIGN KEY (`option_id`)  REFERENCES `vote_option` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生投票记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 预置数据
-- ============================================================

-- 管理员 admin / admin123
INSERT INTO `admin` (`username`, `password`, `real_name`) VALUES
('admin', '$2b$10$WsvfA7.Xmw.f5HuQ9bpBlulDXCZYjqzaSvC9OrpF1YFZ6OTER7t8u', '系统管理员');

-- 学院
INSERT INTO `college` (`college_name`) VALUES
('计算机学院'), ('外国语学院');

-- 专业
INSERT INTO `major` (`college_id`, `major_name`, `major_code`) VALUES
(1, '计算机科学与技术', '080901'),
(1, '软件工程', '080902'),
(2, '英语', '050201');

-- 教师 T001 / 123456
INSERT INTO `teacher` (`teacher_no`, `password`, `name`, `gender`, `phone`, `college_id`, `title`) VALUES
('T001', '$2b$10$ZwCMkmw.9RbpCCrz3vaiPeYvqfwZmywxpzqDQC5ZFlssRdS.Aswg2', '王老师', '男', '13800000001', 1, '副教授'),
('T002', '$2b$10$ZwCMkmw.9RbpCCrz3vaiPeYvqfwZmywxpzqDQC5ZFlssRdS.Aswg2', '李老师', '女', '13800000002', 1, '讲师'),
('T003', '$2b$10$ZwCMkmw.9RbpCCrz3vaiPeYvqfwZmywxpzqDQC5ZFlssRdS.Aswg2', '赵老师', '女', '13800000003', 2, '讲师');

-- 班级
INSERT INTO `class_info` (`class_no`, `class_name`, `major_id`, `teacher_id`, `student_count`) VALUES
('2023-01', '2023级计算机1班', 1, 1, 2),
('2023-02', '2023级计算机2班', 2, 2, 1);

-- 学生(密码=身份证后6位)
INSERT INTO `student` (`student_no`, `password`, `name`, `gender`, `phone`, `id_card`, `class_id`) VALUES
('2023001', '$2b$10$Y9L3t9lD32TDh6TpLwntKuKGg53e6RjUJwvYcqDSOfIi2eFxkI7Ce', '张三', '男', '13900000001', '110101200501011234', 1),
('2023002', '$2b$10$yfuV0O6YmIRrkrD0QM8SReRKFZ8qJG..Rq.lZbgzQe9zVXHz0It7C', '李四', '女', '13900000002', '110101200502022345', 1),
('2023003', '$2b$10$JRMqN.6NO/Mh8MBoKFewUOYRbf/wlJc1AzZ083h/EEjxk5SVdmV6K', '王五', '男', '13900000003', '110101200503033456', 2);

-- 学生干部(复用学生账号): 张三=班长, 李四=学生会(校级)
INSERT INTO `cadre` (`student_id`, `cadre_type`, `class_id`, `union_scope`) VALUES
(1, 'MONITOR', 1, NULL),
(2, 'UNION', NULL, 'SCHOOL');

-- 课程
INSERT INTO `course` (`course_code`, `course_name`, `category_id`, `course_hours`, `credit`, `capacity`, `teacher_id`, `class_time`, `location`, `status`) VALUES
('C001', 'Java程序设计', 5, 64, 4.0, 50, 1, '周一 1-2节', '教2-301', 'OPENING'),
('C002', '数据库原理', 7, 48, 3.0, 50, 2, '周三 3-4节', '教3-205', 'OPENING'),
('C003', '数据结构', 6, 64, 4.0, 50, 1, '周二 1-2节', '教2-310', 'OPENING'),
('C004', '英语一', 1, 48, 2.0, 50, 3, '周五 1-2节', '教1-101', 'OPENING'),
('C005', '高等数学一', 3, 64, 4.0, 50, 2, '周四 3-4节', '教1-201', 'OPENING'),
('C006', 'C语言程序设计', 4, 64, 4.0, 50, 1, '周一 5-6节', '教2-305', 'NOT_STARTED'),
('C007', '操作系统', 8, 48, 3.0, 50, 2, '周三 1-2节', '教2-202', 'NOT_STARTED');

-- 专业计划(计算机科学与技术专业: 第一学年课程)
INSERT INTO `course_category` (`category_code`, `category_name`) VALUES
('080101', '大学英语一'), ('080109', '大学英语二'),
('080201', '高等数学一'), ('080301', '程序设计基础'),
('080302', 'Java程序设计'), ('080303', '数据结构'),
('080304', '数据库原理'), ('080305', '操作系统');

INSERT INTO `major_plan` (`major_id`, `category_id`, `year_level`, `semester`) VALUES
(1, 1, 1, 1), -- 大学英语一
(1, 3, 1, 1), -- 高等数学一
(1, 4, 1, 1), -- 程序设计基础
(1, 5, 1, 2), -- Java程序设计
(1, 7, 1, 2), -- 数据库原理
(2, 1, 1, 1),
(2, 3, 1, 1),
(2, 5, 1, 2),
(2, 6, 1, 2); -- 数据结构

-- 学生选课
INSERT INTO `student_course` (`student_id`, `course_id`) VALUES
(1, 1), (1, 2), (1, 4), (1, 5), (1, 6),
(2, 1), (2, 4), (2, 5), (2, 6),
(3, 1), (3, 3), (3, 4), (3, 5);

-- 成绩(基于选课)
INSERT INTO `score` (`student_id`, `course_id`, `score`, `grade_level`) VALUES
(1, 1, 92.00, '优秀'),
(1, 2, 78.00, '良好'),
(2, 1, 65.00, '及格'),
(3, 1, 88.00, '良好');

-- 文件分类
INSERT INTO `file_category` (`category_name`) VALUES
('文本文件'), ('压缩文件'), ('Word文档'), ('PDF文档'), ('课件');

-- 公告示例
-- 管理员发布学校级公告
INSERT INTO `notice` (`title`, `content`, `scope`, `publisher_role`, `publisher_name`) VALUES
('开学通知', '新学期将于9月1日正式开始，请同学们按时返校注册。', 'SCHOOL', 'ADMIN', '系统管理员');
-- 班长发布班级公告
INSERT INTO `notice` (`title`, `content`, `scope`, `target_class_id`, `publisher_role`, `publisher_name`) VALUES
('班会通知', '本周五下午3点在教2-301召开班会，请全体同学准时参加。', 'CLASS', 1, 'CADRE', '张三');

-- 投票活动: 单票制优秀学生评选(管理员发起)
INSERT INTO `vote` (`title`, `content`, `creator_role`, `creator_name`, `max_votes`, `status`) VALUES
('班级优秀学生评选', '请从以下候选人中选出你心中的优秀学生（每人限投一票）', 'ADMIN', '系统管理员', 1, 1);
INSERT INTO `vote_option` (`vote_id`, `option_text`) VALUES
(1, '张三'), (1, '李四'), (1, '王五');

-- 投票活动: 多票制(每人可投2票), 教师发起
INSERT INTO `vote` (`title`, `content`, `creator_role`, `creator_name`, `teacher_id`, `max_votes`, `status`) VALUES
('课程满意度调查', '请对以下课程分别投票（每人最多投2票）', 'TEACHER', '王老师', 1, 2, 1);
INSERT INTO `vote_option` (`vote_id`, `option_text`) VALUES
(2, 'Java程序设计'), (2, '数据库原理'), (2, '数据结构');

-- 学生投票记录
INSERT INTO `student_vote` (`vote_id`, `student_id`, `option_id`) VALUES
(1, 1, 1),
(1, 2, 3),
(1, 3, 2),
(2, 1, 1),
(2, 1, 2),
(2, 2, 2);
