#!/usr/bin/env python3
"""Document 2: Admin Installation & Deployment Manual"""
import os
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

OUTPUT_DIR = "/home/user/webapp/docs"

def set_cell_bg(cell, color):
    tc = cell._tc
    tcPr = tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), color)
    tcPr.append(shd)

def set_table_borders(table):
    tbl = table._tbl
    tblPr = tbl.find(qn('w:tblPr'))
    if tblPr is None:
        tblPr = OxmlElement('w:tblPr')
        tbl.insert(0, tblPr)
    tblBorders = OxmlElement('w:tblBorders')
    for bn in ['top', 'left', 'bottom', 'right', 'insideH', 'insideV']:
        border = OxmlElement(f'w:{bn}')
        border.set(qn('w:val'), 'single')
        border.set(qn('w:sz'), '4')
        border.set(qn('w:space'), '0')
        border.set(qn('w:color'), '000000')
        tblBorders.append(border)
    tblPr.append(tblBorders)

def page_break(doc):
    p = doc.add_paragraph()
    r = p.add_run()
    br = OxmlElement('w:br')
    br.set(qn('w:type'), 'page')
    r._r.append(br)

def h(doc, text, level=1, color='1F3864'):
    p = doc.add_heading(text, level=level)
    for run in p.runs:
        run.font.color.rgb = RGBColor.from_string(color)
    p.paragraph_format.space_before = Pt(12 if level == 1 else 8)
    p.paragraph_format.space_after = Pt(4)

def p(doc, text, bold=False, indent=0, font_size=11):
    para = doc.add_paragraph()
    para.paragraph_format.space_before = Pt(2)
    para.paragraph_format.space_after = Pt(2)
    if indent:
        para.paragraph_format.left_indent = Cm(indent)
    run = para.add_run(text)
    run.font.name = '宋体'
    run.font.size = Pt(font_size)
    run.font.bold = bold
    return para

def code(doc, text, indent=0.5):
    """Code block style paragraph"""
    para = doc.add_paragraph()
    para.paragraph_format.left_indent = Cm(indent)
    para.paragraph_format.space_before = Pt(2)
    para.paragraph_format.space_after = Pt(2)
    # Add light gray background via shading on paragraph
    pPr = para._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), 'F0F0F0')
    pPr.append(shd)
    run = para.add_run(text)
    run.font.name = 'Courier New'
    run.font.size = Pt(9)
    return para

def note(doc, text):
    """Warning/note box"""
    para = doc.add_paragraph()
    para.paragraph_format.left_indent = Cm(0.5)
    para.paragraph_format.space_before = Pt(3)
    para.paragraph_format.space_after = Pt(3)
    pPr = para._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), 'FFF3CD')
    pPr.append(shd)
    run = para.add_run('⚠ 注意：' + text)
    run.font.size = Pt(10)
    run.font.bold = True

def table2(doc, headers, data, header_color='2E75B6'):
    t = doc.add_table(rows=len(data)+1, cols=len(headers))
    set_table_borders(t)
    for j, h_text in enumerate(headers):
        t.rows[0].cells[j].text = h_text
        set_cell_bg(t.rows[0].cells[j], header_color)
        for para in t.rows[0].cells[j].paragraphs:
            for run in para.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
                run.font.size = Pt(10)
    for i, row in enumerate(data):
        for j, val in enumerate(row):
            t.rows[i+1].cells[j].text = str(val)
            for para in t.rows[i+1].cells[j].paragraphs:
                for run in para.runs:
                    run.font.size = Pt(10)
    return t

def create_doc2():
    doc = Document()
    section = doc.sections[0]
    section.page_width = Cm(21)
    section.page_height = Cm(29.7)
    section.left_margin = Cm(2.5)
    section.right_margin = Cm(2.5)
    section.top_margin = Cm(2.5)
    section.bottom_margin = Cm(2.5)
    
    style = doc.styles['Normal']
    style.font.name = '宋体'
    style.font.size = Pt(11)
    
    # ===== COVER =====
    doc.add_paragraph(); doc.add_paragraph(); doc.add_paragraph()
    
    tp = doc.add_paragraph()
    tp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    tr = tp.add_run('等保项目管理及归档管理系统')
    tr.font.size = Pt(22); tr.font.bold = True
    tr.font.color.rgb = RGBColor(31,56,100); tr.font.name = '黑体'
    
    sp = doc.add_paragraph()
    sp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    sr = sp.add_run('管理员安装部署及维护手册')
    sr.font.size = Pt(18); sr.font.bold = True
    sr.font.color.rgb = RGBColor(31,56,100); sr.font.name = '黑体'
    
    doc.add_paragraph()
    it = doc.add_table(rows=5, cols=2)
    it.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(it)
    for i, (k, v) in enumerate([
        ('编制单位', '南京国云电力有限公司'),
        ('系统版本', 'v2.0'),
        ('文档版本', 'v2.0'),
        ('编制日期', '2026年3月'),
        ('密级', '机密/内部资料'),
    ]):
        it.rows[i].cells[0].text = k
        it.rows[i].cells[1].text = v
        set_cell_bg(it.rows[i].cells[0], 'D6E4F7')
    
    page_break(doc)
    
    # ===== TOC =====
    h(doc, '目录', 1)
    toc_items = [
        ('1', '环境准备与规划', '3'),
        ('1.1', '服务器规划', '3'),
        ('1.2', '软件版本要求', '3'),
        ('1.3', '网络与防火墙规划', '3'),
        ('2', '数据库服务器安装部署（172.16.30.9）', '4'),
        ('2.1', 'MySQL 8 安装', '4'),
        ('2.2', '数据库初始化', '5'),
        ('2.3', '初始化SQL脚本', '5'),
        ('2.4', '数据库安全加固', '7'),
        ('3', '应用服务器安装部署（172.16.40.8）', '8'),
        ('3.1', 'OpenJDK 17 安装', '8'),
        ('3.2', 'Nginx 安装', '8'),
        ('3.3', '自签SSL证书生成', '9'),
        ('3.4', '应用目录与文件准备', '10'),
        ('3.5', '后端应用部署', '10'),
        ('3.6', '前端静态文件部署', '11'),
        ('3.7', 'Nginx配置', '12'),
        ('3.8', 'systemd服务配置', '13'),
        ('3.9', '首次启动与验证', '14'),
        ('4', '安全配置', '15'),
        ('4.1', '重要配置项说明', '15'),
        ('4.2', '安全加固建议', '15'),
        ('5', '日常运维手册', '16'),
        ('5.1', '服务管理命令', '16'),
        ('5.2', '日志查看', '16'),
        ('5.3', '数据库备份与恢复', '17'),
        ('5.4', '应用版本升级', '18'),
        ('5.5', '常见问题排查', '18'),
    ]
    for num, title, page in toc_items:
        para = doc.add_paragraph()
        indent = Cm(0.5 if '.' in num and num.count('.') == 1 else (1.0 if num.count('.') > 1 else 0))
        para.paragraph_format.left_indent = indent
        r1 = para.add_run(f'{num}  {title}')
        r1.font.size = Pt(11)
        r2 = para.add_run(f'    ...  {page}')
        r2.font.size = Pt(11)
    
    page_break(doc)
    
    # ===== CHAPTER 1: ENVIRONMENT PLANNING =====
    h(doc, '1  环境准备与规划', 1)
    
    h(doc, '1.1  服务器规划', 2)
    table2(doc, ['服务器角色', 'IP地址', '操作系统', '最低配置', '主要软件'],
    [
        ('数据库服务器', '172.16.30.9', 'CentOS 8 Stream', 'CPU: 4核\n内存: 8GB\n磁盘: 100GB', 'MySQL 8.0'),
        ('应用服务器', '172.16.40.8', 'CentOS 8 Stream', 'CPU: 4核\n内存: 8GB\n磁盘: 100GB', 'OpenJDK 17\nNginx\nSpring Boot'),
    ], '1F3864')
    
    h(doc, '1.2  软件版本要求', 2)
    table2(doc, ['软件', '版本要求', '说明'],
    [
        ('CentOS', '8 Stream', '操作系统'),
        ('OpenJDK', '17 (LTS)', 'Java运行环境，必须版本17+'),
        ('MySQL', '8.0.x', '数据库服务'),
        ('Nginx', '1.20+', 'Web服务器和反向代理'),
        ('OpenSSL', '1.1.1+', 'SSL证书生成'),
    ])
    
    h(doc, '1.3  网络与防火墙规划', 2)
    p(doc, '应用服务器需开放以下端口：')
    table2(doc, ['端口', '协议', '说明', '来源'],
    [
        ('443', 'HTTPS/TCP', 'Nginx HTTPS访问', '用户终端'),
        ('80', 'HTTP/TCP', 'HTTP跳转至HTTPS（可选）', '用户终端'),
        ('8080', 'HTTP/TCP', 'Spring Boot API（仅内网）', '本机/内网'),
        ('22', 'SSH/TCP', '管理员远程连接', '运维IP'),
    ])
    doc.add_paragraph()
    p(doc, '数据库服务器需开放以下端口：')
    table2(doc, ['端口', '协议', '说明', '来源'],
    [
        ('3306', 'TCP', 'MySQL数据库', '仅允许172.16.40.8'),
        ('22', 'SSH/TCP', '管理员远程连接', '运维IP'),
    ])
    doc.add_paragraph()
    note(doc, '生产环境中，3306端口绝对不应对公网开放，只允许应用服务器IP访问。')
    
    page_break(doc)
    
    # ===== CHAPTER 2: DATABASE SERVER =====
    h(doc, '2  数据库服务器安装部署（172.16.30.9）', 1)
    
    h(doc, '2.1  MySQL 8 安装', 2)
    p(doc, '在数据库服务器（172.16.30.9）上执行以下命令：')
    p(doc, '步骤1：添加MySQL官方Yum仓库', bold=True)
    code(doc, '# 下载MySQL 8.0 Yum仓库配置RPM包')
    code(doc, 'wget https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm')
    code(doc, 'sudo rpm -ivh mysql80-community-release-el8-9.noarch.rpm')
    code(doc, '# 或者使用阿里云镜像（国内推荐）')
    code(doc, 'sudo dnf install -y https://repo.mysql.com/mysql80-community-release-el8-9.noarch.rpm')
    
    p(doc, '步骤2：安装MySQL Server', bold=True)
    code(doc, '# 禁用CentOS 8默认MySQL模块，启用MySQL官方源')
    code(doc, 'sudo dnf module disable mysql -y')
    code(doc, 'sudo dnf install -y mysql-community-server')
    
    p(doc, '步骤3：启动并设置开机自启', bold=True)
    code(doc, 'sudo systemctl start mysqld')
    code(doc, 'sudo systemctl enable mysqld')
    code(doc, 'sudo systemctl status mysqld')
    
    p(doc, '步骤4：获取临时密码并修改root密码', bold=True)
    code(doc, '# 获取MySQL初始临时密码')
    code(doc, 'sudo grep "temporary password" /var/log/mysqld.log')
    code(doc, '# 示例输出: [Note] A temporary password is generated for root@localhost: xxxx')
    code(doc, '')
    code(doc, '# 安全初始化')
    code(doc, 'sudo mysql_secure_installation')
    code(doc, '# 按提示修改root密码，建议删除匿名用户，禁止root远程登录，删除测试数据库')
    
    p(doc, '步骤5：验证安装', bold=True)
    code(doc, 'mysql -u root -p')
    code(doc, 'mysql> SELECT VERSION();')
    code(doc, '# 应显示: 8.0.xx 版本号')
    
    h(doc, '2.2  数据库初始化', 2)
    p(doc, '步骤1：创建应用数据库和专用用户', bold=True)
    code(doc, 'mysql -u root -p')
    code(doc, '-- 创建数据库（UTF8MB4编码，支持中文及emoji）')
    code(doc, "CREATE DATABASE djbh_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    code(doc, '-- 创建应用专用数据库用户（仅允许来自应用服务器的连接）')
    code(doc, "CREATE USER 'djbh_user'@'172.16.40.8' IDENTIFIED BY 'YourStrongPassword@2026!';")
    code(doc, "GRANT ALL PRIVILEGES ON djbh_system.* TO 'djbh_user'@'172.16.40.8';")
    code(doc, 'FLUSH PRIVILEGES;')
    code(doc, 'EXIT;')
    doc.add_paragraph()
    note(doc, '请将 YourStrongPassword@2026! 替换为强密码（至少12位，包含大小写字母、数字和特殊字符）。')
    
    p(doc, '步骤2：导入初始化SQL', bold=True)
    p(doc, '将系统提供的初始化SQL文件（init.sql）上传到数据库服务器，执行导入：')
    code(doc, 'mysql -u root -p djbh_system < /tmp/init.sql')
    
    h(doc, '2.3  初始化SQL脚本参考', 2)
    p(doc, '以下为系统核心表的初始化SQL结构（以t_user和t_role为例）：')
    
    code(doc, '-- 角色表')
    code(doc, "CREATE TABLE IF NOT EXISTS `t_role` (")
    code(doc, "  `id` bigint NOT NULL AUTO_INCREMENT,")
    code(doc, "  `role_code` varchar(50) NOT NULL,")
    code(doc, "  `role_name` varchar(100) NOT NULL,")
    code(doc, "  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,")
    code(doc, "  PRIMARY KEY (`id`)")
    code(doc, ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
    code(doc, '')
    code(doc, "-- 插入基础角色")
    code(doc, "INSERT INTO t_role (role_code, role_name) VALUES")
    code(doc, "  ('ROLE_ADMIN', '系统管理员'),")
    code(doc, "  ('ROLE_MANAGER', '项目经理'),")
    code(doc, "  ('ROLE_STAFF', '普通用户');")
    code(doc, '')
    code(doc, '-- 系统用户表')
    code(doc, "CREATE TABLE IF NOT EXISTS `t_user` (")
    code(doc, "  `id` bigint NOT NULL AUTO_INCREMENT,")
    code(doc, "  `username` varchar(50) NOT NULL,")
    code(doc, "  `real_name` varchar(500),  -- SM4加密")
    code(doc, "  `password_hash` varchar(200) NOT NULL,")
    code(doc, "  `role_id` bigint NOT NULL,")
    code(doc, "  `totp_enabled` tinyint DEFAULT 0,")
    code(doc, "  `status` tinyint DEFAULT 1,")
    code(doc, "  `first_login` tinyint DEFAULT 1,")
    code(doc, "  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,")
    code(doc, "  PRIMARY KEY (`id`),")
    code(doc, "  UNIQUE KEY `uk_username` (`username`)")
    code(doc, ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
    code(doc, '')
    code(doc, '-- 插入默认管理员账户（密码: Admin@123456，首次登录须修改）')
    code(doc, "-- 以下password_hash为Admin@123456的BCrypt哈希，请在首次登录后立即修改！")
    code(doc, "INSERT INTO t_user (username, password_hash, role_id, first_login) VALUES")
    code(doc, "  ('admin', '$2a$10$...BcryptHashHere...', 1, 1);")
    doc.add_paragraph()
    note(doc, '完整的初始化SQL脚本由项目部署包提供，包含所有18张数据表的建表语句及初始数据。')
    
    h(doc, '2.4  数据库安全加固', 2)
    p(doc, '完成安装后进行以下安全加固：')
    
    p(doc, '步骤1：修改MySQL配置文件', bold=True)
    code(doc, 'sudo vi /etc/my.cnf')
    code(doc, '# 在 [mysqld] 节添加以下配置：')
    code(doc, '[mysqld]')
    code(doc, 'bind-address = 0.0.0.0  # 允许网络连接（通过防火墙控制访问来源）')
    code(doc, 'port = 3306')
    code(doc, 'character-set-server = utf8mb4')
    code(doc, 'collation-server = utf8mb4_unicode_ci')
    code(doc, 'max_connections = 200')
    code(doc, 'innodb_buffer_pool_size = 2G  # 根据实际内存调整')
    code(doc, 'slow_query_log = 1')
    code(doc, 'slow_query_log_file = /var/log/mysql/slow-queries.log')
    code(doc, 'long_query_time = 2  # 超过2秒的查询记录慢日志')
    
    p(doc, '步骤2：重启MySQL并设置防火墙', bold=True)
    code(doc, 'sudo systemctl restart mysqld')
    code(doc, '# 仅允许应用服务器IP访问3306端口')
    code(doc, 'sudo firewall-cmd --permanent --add-rich-rule="rule family=ipv4 source address=172.16.40.8 port protocol=tcp port=3306 accept"')
    code(doc, 'sudo firewall-cmd --reload')
    
    page_break(doc)
    
    # ===== CHAPTER 3: APP SERVER =====
    h(doc, '3  应用服务器安装部署（172.16.40.8）', 1)
    
    h(doc, '3.1  OpenJDK 17 安装', 2)
    code(doc, '# 安装OpenJDK 17')
    code(doc, 'sudo dnf install -y java-17-openjdk java-17-openjdk-devel')
    code(doc, '')
    code(doc, '# 验证安装')
    code(doc, 'java -version')
    code(doc, '# 期望输出: openjdk version "17.x.x" ...')
    code(doc, '')
    code(doc, '# 如果系统有多个JDK，设置默认版本')
    code(doc, 'sudo alternatives --config java')
    code(doc, '# 选择 java-17-openjdk 对应的编号')
    code(doc, '')
    code(doc, '# 永久设置JAVA_HOME（可选，Spring Boot不需要）')
    code(doc, 'echo "export JAVA_HOME=$(java -XshowSettings:all -version 2>&1 | grep java.home | awk \'{print $3}\')" >> ~/.bashrc')
    code(doc, 'source ~/.bashrc')
    
    h(doc, '3.2  Nginx 安装', 2)
    code(doc, '# 安装EPEL源（提供Nginx）')
    code(doc, 'sudo dnf install -y epel-release')
    code(doc, 'sudo dnf install -y nginx')
    code(doc, '')
    code(doc, '# 启动并设置开机自启')
    code(doc, 'sudo systemctl start nginx')
    code(doc, 'sudo systemctl enable nginx')
    code(doc, 'sudo systemctl status nginx')
    code(doc, '')
    code(doc, '# 配置防火墙允许HTTP/HTTPS')
    code(doc, 'sudo firewall-cmd --permanent --add-service=http')
    code(doc, 'sudo firewall-cmd --permanent --add-service=https')
    code(doc, 'sudo firewall-cmd --reload')
    
    h(doc, '3.3  自签SSL证书生成', 2)
    p(doc, '在应用服务器上生成自签名SSL证书（域名：njgy.dbcp.cn）：')
    p(doc, '步骤1：创建证书目录', bold=True)
    code(doc, 'sudo mkdir -p /opt/djbh/ssl')
    code(doc, 'cd /opt/djbh/ssl')
    
    p(doc, '步骤2：生成私钥和证书', bold=True)
    code(doc, '# 生成2048位RSA私钥')
    code(doc, 'sudo openssl genrsa -out njgy.dbcp.cn.key 2048')
    code(doc, '')
    code(doc, '# 生成证书签名请求（CSR）')
    code(doc, 'sudo openssl req -new -key njgy.dbcp.cn.key -out njgy.dbcp.cn.csr \\')
    code(doc, '  -subj "/C=CN/ST=Jiangsu/L=Nanjing/O=NanJingGuoyunDianli/CN=njgy.dbcp.cn"')
    code(doc, '')
    code(doc, '# 生成自签名证书（有效期10年）')
    code(doc, 'sudo openssl x509 -req -days 3650 \\')
    code(doc, '  -in njgy.dbcp.cn.csr \\')
    code(doc, '  -signkey njgy.dbcp.cn.key \\')
    code(doc, '  -out njgy.dbcp.cn.crt')
    code(doc, '')
    code(doc, '# 设置权限保护私钥')
    code(doc, 'sudo chmod 600 njgy.dbcp.cn.key')
    code(doc, 'sudo ls -la /opt/djbh/ssl/')
    code(doc, '# 应看到: njgy.dbcp.cn.crt  njgy.dbcp.cn.csr  njgy.dbcp.cn.key')
    
    doc.add_paragraph()
    note(doc, '自签名证书浏览器会提示"不安全"，需手动信任。如需正式证书，可申请Let\'s Encrypt免费证书或购买CA签发证书。')
    
    h(doc, '3.4  应用目录与文件准备', 2)
    p(doc, '创建应用部署目录结构：')
    code(doc, '# 创建应用根目录及子目录')
    code(doc, 'sudo mkdir -p /opt/djbh/{backend,frontend,data/templates,data/archives,logs,ssl}')
    code(doc, '')
    code(doc, '# 创建应用运行用户（不建议使用root运行应用）')
    code(doc, 'sudo useradd -r -s /bin/false djbh')
    code(doc, 'sudo chown -R djbh:djbh /opt/djbh')
    code(doc, '')
    code(doc, '# 目录结构说明：')
    code(doc, '# /opt/djbh/backend/     - Spring Boot JAR包')
    code(doc, '# /opt/djbh/frontend/    - 前端构建产物（dist目录内容）')
    code(doc, '# /opt/djbh/data/        - 应用数据（模板、归档文件）')
    code(doc, '# /opt/djbh/logs/        - 应用日志')
    code(doc, '# /opt/djbh/ssl/         - SSL证书')
    
    h(doc, '3.5  后端应用部署', 2)
    p(doc, '步骤1：上传JAR包', bold=True)
    p(doc, '将编译好的后端JAR包（djbh-system-v2.0.jar）上传到应用服务器：')
    code(doc, '# 在本地开发机执行（或使用FTP/SFTP工具上传）')
    code(doc, 'scp djbh-system-v2.0.jar user@172.16.40.8:/opt/djbh/backend/')
    
    p(doc, '步骤2：创建应用配置文件', bold=True)
    p(doc, '在 /opt/djbh/backend/ 下创建 application-prod.yml（生产环境配置文件）：')
    code(doc, 'sudo vi /opt/djbh/backend/application-prod.yml')
    doc.add_paragraph()
    code(doc, '# 生产环境配置文件内容：')
    code(doc, 'spring:')
    code(doc, '  datasource:')
    code(doc, '    url: jdbc:mysql://172.16.30.9:3306/djbh_system?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8')
    code(doc, '    username: djbh_user')
    code(doc, '    password: YourStrongPassword@2026!')
    code(doc, '    driver-class-name: com.mysql.cj.jdbc.Driver')
    code(doc, '')
    code(doc, 'app:')
    code(doc, '  jwt-secret: YOUR_STRONG_JWT_SECRET_AT_LEAST_64_CHARS_ABCDEFGHIJKLMNOPQRSTUVWXYZ')
    code(doc, '  jwt-expiration: 28800')
    code(doc, '  crypto:')
    code(doc, '    root-key: YOUR_STRONG_ROOT_KEY_AT_LEAST_32_CHARS_ABCDEF')
    code(doc, '  file:')
    code(doc, '    template-path: /opt/djbh/data/templates/')
    code(doc, '    archive-path: /opt/djbh/data/archives/')
    code(doc, '')
    code(doc, 'logging:')
    code(doc, '  file:')
    code(doc, '    name: /opt/djbh/logs/djbh-system.log')
    doc.add_paragraph()
    note(doc, '请务必修改 app.jwt-secret 和 app.crypto.root-key 为强随机字符串。jwt-secret需至少64字符，root-key需至少32字符。这两个密钥一旦设置后不能随意修改，否则会导致现有Token失效和加密数据无法解密。')
    
    p(doc, '步骤3：设置配置文件权限', bold=True)
    code(doc, 'sudo chown djbh:djbh /opt/djbh/backend/application-prod.yml')
    code(doc, 'sudo chmod 600 /opt/djbh/backend/application-prod.yml')
    code(doc, '# 仅djbh用户可读，保护密码和密钥')
    
    h(doc, '3.6  前端静态文件部署', 2)
    p(doc, '步骤1：获取前端构建产物', bold=True)
    p(doc, '将前端构建目录（dist/）的内容上传到应用服务器：')
    code(doc, '# 在本地开发机上构建前端（如未提供构建产物）')
    code(doc, 'cd frontend')
    code(doc, 'npm install')
    code(doc, 'npm run build')
    code(doc, '# 构建产物位于 dist/ 目录')
    code(doc, '')
    code(doc, '# 上传到服务器')
    code(doc, 'scp -r dist/* user@172.16.40.8:/opt/djbh/frontend/')
    
    p(doc, '步骤2：验证文件', bold=True)
    code(doc, 'ls /opt/djbh/frontend/')
    code(doc, '# 应看到: index.html  assets/  favicon.svg 等文件')
    
    h(doc, '3.7  Nginx配置', 2)
    p(doc, '创建Nginx配置文件：')
    code(doc, 'sudo vi /etc/nginx/conf.d/djbh.conf')
    doc.add_paragraph()
    code(doc, '# HTTP -> HTTPS 重定向')
    code(doc, 'server {')
    code(doc, '    listen 80;')
    code(doc, '    server_name njgy.dbcp.cn;')
    code(doc, '    return 301 https://$host$request_uri;')
    code(doc, '}')
    code(doc, '')
    code(doc, '# HTTPS 主配置')
    code(doc, 'server {')
    code(doc, '    listen 443 ssl http2;')
    code(doc, '    server_name njgy.dbcp.cn;')
    code(doc, '')
    code(doc, '    # SSL证书')
    code(doc, '    ssl_certificate     /opt/djbh/ssl/njgy.dbcp.cn.crt;')
    code(doc, '    ssl_certificate_key /opt/djbh/ssl/njgy.dbcp.cn.key;')
    code(doc, '    ssl_protocols TLSv1.2 TLSv1.3;')
    code(doc, '    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;')
    code(doc, '    ssl_prefer_server_ciphers on;')
    code(doc, '    ssl_session_cache shared:SSL:10m;')
    code(doc, '')
    code(doc, '    # 文件上传大小限制（与后端保持一致）')
    code(doc, '    client_max_body_size 100m;')
    code(doc, '')
    code(doc, '    # 前端静态文件')
    code(doc, '    location / {')
    code(doc, '        root /opt/djbh/frontend;')
    code(doc, '        index index.html;')
    code(doc, '        try_files $uri $uri/ /index.html;  # SPA路由支持')
    code(doc, '    }')
    code(doc, '')
    code(doc, '    # 后端API反向代理')
    code(doc, '    location /api/ {')
    code(doc, '        proxy_pass http://127.0.0.1:8080;')
    code(doc, '        proxy_set_header Host $host;')
    code(doc, '        proxy_set_header X-Real-IP $remote_addr;')
    code(doc, '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;')
    code(doc, '        proxy_set_header X-Forwarded-Proto $scheme;')
    code(doc, '        proxy_connect_timeout 30s;')
    code(doc, '        proxy_read_timeout 300s;  # 归档生成可能较慢')
    code(doc, '        proxy_send_timeout 60s;')
    code(doc, '    }')
    code(doc, '')
    code(doc, '    # 安全Headers')
    code(doc, '    add_header X-Frame-Options SAMEORIGIN;')
    code(doc, '    add_header X-Content-Type-Options nosniff;')
    code(doc, '    add_header X-XSS-Protection "1; mode=block";')
    code(doc, '    add_header Strict-Transport-Security "max-age=31536000" always;')
    code(doc, '}')
    doc.add_paragraph()
    p(doc, '测试并重载Nginx配置：')
    code(doc, 'sudo nginx -t')
    code(doc, '# 期望输出: nginx: configuration file /etc/nginx/nginx.conf test is successful')
    code(doc, 'sudo systemctl reload nginx')
    
    h(doc, '3.8  systemd服务配置', 2)
    p(doc, '创建Spring Boot应用的systemd服务单元文件：')
    code(doc, 'sudo vi /etc/systemd/system/djbh-backend.service')
    doc.add_paragraph()
    code(doc, '[Unit]')
    code(doc, 'Description=等保项目管理及归档管理系统 - Backend')
    code(doc, 'Documentation=https://njgy.dbcp.cn')
    code(doc, 'After=network.target mysql.service')
    code(doc, 'Wants=network.target')
    code(doc, '')
    code(doc, '[Service]')
    code(doc, 'Type=simple')
    code(doc, 'User=djbh')
    code(doc, 'Group=djbh')
    code(doc, 'WorkingDirectory=/opt/djbh/backend')
    code(doc, 'EnvironmentFile=/opt/djbh/backend/.env  # 可选：从.env文件读取环境变量')
    code(doc, '')
    code(doc, '# 启动命令')
    code(doc, 'ExecStart=/usr/bin/java \\')
    code(doc, '    -Xms512m -Xmx2g \\')
    code(doc, '    -XX:+UseG1GC \\')
    code(doc, '    -Djava.security.egd=file:/dev/./urandom \\')
    code(doc, '    -Dspring.profiles.active=prod \\')
    code(doc, '    -Dspring.config.additional-location=/opt/djbh/backend/application-prod.yml \\')
    code(doc, '    -jar /opt/djbh/backend/djbh-system-v2.0.jar')
    code(doc, '')
    code(doc, '# 重启策略')
    code(doc, 'Restart=on-failure')
    code(doc, 'RestartSec=10s')
    code(doc, 'StartLimitIntervalSec=60')
    code(doc, 'StartLimitBurst=3')
    code(doc, '')
    code(doc, '# 日志')
    code(doc, 'StandardOutput=journal')
    code(doc, 'StandardError=journal')
    code(doc, 'SyslogIdentifier=djbh-backend')
    code(doc, '')
    code(doc, '[Install]')
    code(doc, 'WantedBy=multi-user.target')
    doc.add_paragraph()
    p(doc, '重新加载systemd并启用服务：')
    code(doc, 'sudo systemctl daemon-reload')
    code(doc, 'sudo systemctl enable djbh-backend')
    code(doc, 'sudo systemctl start djbh-backend')
    code(doc, 'sudo systemctl status djbh-backend')
    
    h(doc, '3.9  首次启动与验证', 2)
    p(doc, '步骤1：检查应用启动状态', bold=True)
    code(doc, '# 查看服务状态')
    code(doc, 'sudo systemctl status djbh-backend')
    code(doc, '')
    code(doc, '# 查看最近日志')
    code(doc, 'sudo journalctl -u djbh-backend -n 50 --no-pager')
    code(doc, '# 期望看到: "Started DjbhSystemApplication" 字样')
    code(doc, '')
    code(doc, '# 测试后端API是否正常响应')
    code(doc, 'curl -k https://localhost:443/api/auth/captcha')
    code(doc, '# 期望返回JSON格式的验证码数据')
    
    p(doc, '步骤2：浏览器访问验证', bold=True)
    p(doc, '在用户终端浏览器访问 https://njgy.dbcp.cn，应能看到系统登录页面。')
    p(doc, '首次访问时浏览器会提示证书不受信任（因为是自签名证书），需手动选择"继续访问"或将证书添加到受信任的证书列表。')
    
    p(doc, '步骤3：使用默认管理员账户登录', bold=True)
    table2(doc, ['默认账户', '默认密码', '备注'],
    [
        ('admin', 'Admin@123456', '系统管理员，首次登录系统会要求强制修改密码'),
    ])
    doc.add_paragraph()
    note(doc, '强烈建议首次登录后立即修改默认密码！默认密码 Admin@123456 仅供初次登录使用。')
    
    page_break(doc)
    
    # ===== CHAPTER 4: SECURITY CONFIG =====
    h(doc, '4  安全配置', 1)
    
    h(doc, '4.1  重要配置项说明', 2)
    p(doc, '生产环境中，以下配置项在 application-prod.yml 中必须修改：')
    table2(doc, ['配置项', '描述', '要求'],
    [
        ('app.jwt-secret', 'JWT签名密钥，用于生成和验证用户会话Token', '至少64位随机字符串，使用大小写字母+数字+特殊字符，**切勿使用默认值**'),
        ('app.crypto.root-key', 'SM4数据加密根密钥，用于加密数据库中的敏感字段', '至少32位随机字符串，一旦设定不可更改（否则已加密数据无法解密）'),
        ('spring.datasource.password', '数据库连接密码', '使用步骤2.2中设置的强密码'),
    ], '1F3864')
    doc.add_paragraph()
    p(doc, '生成随机密钥的方法：')
    code(doc, '# 生成64位随机字符串（适用于jwt-secret）')
    code(doc, "openssl rand -base64 64 | tr -d '\\n'")
    code(doc, '')
    code(doc, '# 生成32位随机字符串（适用于root-key）')
    code(doc, "openssl rand -base64 32 | tr -d '\\n'")
    
    h(doc, '4.2  安全加固建议', 2)
    security_items = [
        ('操作系统', 'CentOS 8及时安装安全更新：sudo dnf update -y --security'),
        ('SSH安全', '修改SSH默认端口22为其他端口，禁用root直接SSH登录，使用密钥认证代替密码认证'),
        ('防火墙', '严格配置firewalld，只开放必要端口（80、443、22），禁止数据库端口3306对公网开放'),
        ('文件权限', '确保应用配置文件（application-prod.yml）权限为600，私钥文件权限为600'),
        ('数据库', '禁用MySQL root账户的远程登录，使用最小权限的应用专用账户'),
        ('Nginx', '禁用不安全的TLS版本（TLS1.0/1.1），开启HSTS响应头'),
        ('日志监控', '定期检查系统操作日志和登录日志，异常登录尝试应及时处理'),
        ('定期备份', '每日自动备份MySQL数据库，备份文件加密后存储到独立的备份服务器'),
    ]
    sec_table = doc.add_table(rows=len(security_items)+1, cols=2)
    set_table_borders(sec_table)
    sec_table.rows[0].cells[0].text = '安全类别'
    sec_table.rows[0].cells[1].text = '建议措施'
    set_cell_bg(sec_table.rows[0].cells[0], '1F3864')
    set_cell_bg(sec_table.rows[0].cells[1], '1F3864')
    for c in sec_table.rows[0].cells:
        for para in c.paragraphs:
            for run in para.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (k, v) in enumerate(security_items):
        sec_table.rows[i+1].cells[0].text = k
        sec_table.rows[i+1].cells[1].text = v
    
    page_break(doc)
    
    # ===== CHAPTER 5: MAINTENANCE =====
    h(doc, '5  日常运维手册', 1)
    
    h(doc, '5.1  服务管理命令', 2)
    p(doc, '后端应用服务管理（在应用服务器 172.16.40.8 上执行）：')
    table2(doc, ['操作', '命令', '说明'],
    [
        ('启动后端', 'sudo systemctl start djbh-backend', '启动Spring Boot应用'),
        ('停止后端', 'sudo systemctl stop djbh-backend', '优雅停止应用'),
        ('重启后端', 'sudo systemctl restart djbh-backend', '重启应用（升级后使用）'),
        ('查看状态', 'sudo systemctl status djbh-backend', '查看运行状态'),
        ('启动Nginx', 'sudo systemctl start nginx', '启动Nginx'),
        ('重载Nginx配置', 'sudo systemctl reload nginx', '不中断服务重载配置'),
        ('测试Nginx配置', 'sudo nginx -t', '验证配置文件语法'),
        ('查看Nginx错误', 'sudo tail -f /var/log/nginx/error.log', '实时查看错误日志'),
    ])
    
    h(doc, '5.2  日志查看', 2)
    p(doc, '应用日志：')
    code(doc, '# 实时查看应用日志（最近50行）')
    code(doc, 'sudo journalctl -u djbh-backend -n 50 -f')
    code(doc, '')
    code(doc, '# 查看日志文件（按日期分片）')
    code(doc, 'tail -f /opt/djbh/logs/djbh-system.log')
    code(doc, '')
    code(doc, '# 搜索特定关键字（如ERROR）')
    code(doc, 'grep "ERROR" /opt/djbh/logs/djbh-system.log | tail -20')
    code(doc, '')
    code(doc, '# 查看应用启动时间')
    code(doc, 'sudo journalctl -u djbh-backend --since "2026-03-01" | grep "Started"')
    
    p(doc, 'Nginx访问日志：')
    code(doc, '# 实时查看Nginx访问日志')
    code(doc, 'sudo tail -f /var/log/nginx/access.log')
    code(doc, '')
    code(doc, '# 统计今日访问次数')
    code(doc, 'sudo cat /var/log/nginx/access.log | grep "$(date +%d/%b/%Y)" | wc -l')
    
    p(doc, 'MySQL日志：')
    code(doc, '# 查看MySQL错误日志')
    code(doc, 'sudo tail -f /var/log/mysqld.log')
    code(doc, '')
    code(doc, '# 查看慢查询日志')
    code(doc, 'sudo tail -f /var/log/mysql/slow-queries.log')
    
    h(doc, '5.3  数据库备份与恢复', 2)
    p(doc, '手动备份：')
    code(doc, '# 在数据库服务器(172.16.30.9)上执行')
    code(doc, '# 备份整个数据库')
    code(doc, 'mysqldump -u djbh_user -p djbh_system | gzip > /backup/djbh_$(date +%Y%m%d_%H%M%S).sql.gz')
    
    p(doc, '配置自动备份（crontab）：')
    code(doc, '# 创建备份脚本')
    code(doc, 'sudo vi /opt/backup/backup_djbh.sh')
    code(doc, '')
    code(doc, '#!/bin/bash')
    code(doc, 'BACKUP_DIR="/backup/djbh"')
    code(doc, 'DATE=$(date +%Y%m%d_%H%M%S)')
    code(doc, 'mkdir -p $BACKUP_DIR')
    code(doc, 'mysqldump -u djbh_user -p"YourStrongPassword@2026!" djbh_system | gzip > $BACKUP_DIR/djbh_$DATE.sql.gz')
    code(doc, '# 保留最近30天备份，删除旧文件')
    code(doc, 'find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete')
    code(doc, 'echo "Backup completed: djbh_$DATE.sql.gz"')
    code(doc, '')
    code(doc, 'sudo chmod +x /opt/backup/backup_djbh.sh')
    code(doc, '')
    code(doc, '# 添加每日凌晨2:00自动备份')
    code(doc, 'sudo crontab -e')
    code(doc, '# 添加以下一行：')
    code(doc, '0 2 * * * /opt/backup/backup_djbh.sh >> /var/log/djbh_backup.log 2>&1')
    
    p(doc, '数据恢复：')
    code(doc, '# 停止应用服务（防止恢复期间写入）')
    code(doc, '# 在应用服务器执行：')
    code(doc, 'sudo systemctl stop djbh-backend')
    code(doc, '')
    code(doc, '# 在数据库服务器执行恢复：')
    code(doc, 'gunzip < /backup/djbh/djbh_20260301_020000.sql.gz | mysql -u root -p djbh_system')
    code(doc, '')
    code(doc, '# 恢复完成后重启应用')
    code(doc, 'sudo systemctl start djbh-backend')
    
    h(doc, '5.4  应用版本升级', 2)
    p(doc, '当需要升级后端应用时，按以下步骤操作：')
    code(doc, '# 1. 备份当前JAR包')
    code(doc, 'sudo cp /opt/djbh/backend/djbh-system-v2.0.jar /opt/djbh/backend/djbh-system-v2.0.jar.bak')
    code(doc, '')
    code(doc, '# 2. 上传新JAR包')
    code(doc, 'scp djbh-system-v2.1.jar user@172.16.40.8:/opt/djbh/backend/')
    code(doc, '')
    code(doc, '# 3. 修改systemd配置中的JAR包路径（如有变更）')
    code(doc, 'sudo vi /etc/systemd/system/djbh-backend.service')
    code(doc, '# 修改 ExecStart 中的JAR包文件名')
    code(doc, 'sudo systemctl daemon-reload')
    code(doc, '')
    code(doc, '# 4. 重启服务')
    code(doc, 'sudo systemctl restart djbh-backend')
    code(doc, 'sudo systemctl status djbh-backend')
    code(doc, '')
    code(doc, '# 5. 若新版本有数据库变更，先执行数据库升级脚本')
    code(doc, 'mysql -u djbh_user -p djbh_system < /tmp/migration_v2.1.sql')
    
    p(doc, '前端升级：')
    code(doc, '# 1. 备份当前前端文件（可选）')
    code(doc, 'sudo tar -czf /opt/djbh/frontend_backup_$(date +%Y%m%d).tar.gz /opt/djbh/frontend/')
    code(doc, '')
    code(doc, '# 2. 上传新的前端构建产物')
    code(doc, 'scp -r dist/* user@172.16.40.8:/opt/djbh/frontend/')
    code(doc, '')
    code(doc, '# 3. 清理Nginx缓存（如有配置缓存）')
    code(doc, 'sudo systemctl reload nginx')
    
    h(doc, '5.5  常见问题排查', 2)
    qa_data = [
        ('后端服务启动失败', 
         '查看日志：sudo journalctl -u djbh-backend -n 100\n常见原因：\n1. 数据库连接失败（检查172.16.30.9:3306是否可达，账号密码是否正确）\n2. 端口8080被占用（netstat -tlnp | grep 8080）\n3. Java版本不符（java -version 需显示17+）\n4. JAR包文件损坏（重新上传）'),
        ('前端页面404/空白',
         '检查/opt/djbh/frontend/目录下是否有index.html；\n检查Nginx配置中try_files是否配置；\n执行nginx -t验证配置；\n查看Nginx错误日志。'),
        ('API接口返回502错误',
         '说明Nginx无法连接到后端8080端口；\n检查后端是否正常运行（systemctl status djbh-backend）；\n检查防火墙配置（8080端口需在本机可访问）。'),
        ('登录后Token失效（频繁重新登录）',
         '检查app.jwt-expiration配置（单位：秒，默认28800=8小时）；\n检查服务器时间是否准确（date命令查看，使用chronyd同步时间）。'),
        ('归档材料生成超时',
         '增大Nginx的proxy_read_timeout配置（已设为300秒）；\n检查/opt/djbh/data/目录是否有写入权限（chown -R djbh:djbh /opt/djbh/data）。'),
        ('SSL证书告警',
         '自签名证书属于正常现象，用户需手动信任；\n如需消除告警，申请CA签发的正式证书（Let\'s Encrypt免费）替换。'),
    ]
    qa_table = doc.add_table(rows=len(qa_data)+1, cols=2)
    set_table_borders(qa_table)
    qa_table.rows[0].cells[0].text = '问题现象'
    qa_table.rows[0].cells[1].text = '排查方法'
    set_cell_bg(qa_table.rows[0].cells[0], '1F3864')
    set_cell_bg(qa_table.rows[0].cells[1], '1F3864')
    for c in qa_table.rows[0].cells:
        for para_obj in c.paragraphs:
            for run in para_obj.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (q, a) in enumerate(qa_data):
        qa_table.rows[i+1].cells[0].text = q
        qa_table.rows[i+1].cells[1].text = a
        for para_obj in qa_table.rows[i+1].cells[1].paragraphs:
            for run in para_obj.runs:
                run.font.size = Pt(9)
    
    # Save
    output_path = os.path.join(OUTPUT_DIR, '文档2-管理员安装部署及维护手册.docx')
    doc.save(output_path)
    print(f"Doc2 saved: {output_path}")
    print(f"File size: {os.path.getsize(output_path)} bytes")

create_doc2()
