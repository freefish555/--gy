#!/usr/bin/env python3
"""Document 3: User Manual"""
import os
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

SCREENSHOTS_DIR = "/home/user/webapp/docs/screenshots"
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
    pg = doc.add_paragraph()
    r = pg.add_run()
    br = OxmlElement('w:br')
    br.set(qn('w:type'), 'page')
    r._r.append(br)

def h(doc, text, level=1, color='1F3864'):
    pg = doc.add_heading(text, level=level)
    for run in pg.runs:
        run.font.color.rgb = RGBColor.from_string(color)
    pg.paragraph_format.space_before = Pt(12 if level == 1 else 8)
    pg.paragraph_format.space_after = Pt(4)

def tx(doc, text, bold=False, indent=0, size=11):
    pg = doc.add_paragraph()
    pg.paragraph_format.space_before = Pt(2)
    pg.paragraph_format.space_after = Pt(3)
    if indent:
        pg.paragraph_format.left_indent = Cm(indent)
    run = pg.add_run(text)
    run.font.name = '宋体'
    run.font.size = Pt(size)
    run.font.bold = bold
    return pg

def tip(doc, text, tip_type='tip'):
    """Info/tip box"""
    color = 'D1ECF1' if tip_type == 'tip' else 'FFF3CD'
    pg = doc.add_paragraph()
    pg.paragraph_format.left_indent = Cm(0.5)
    pPr = pg._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), color)
    pPr.append(shd)
    prefix = '💡 提示：' if tip_type == 'tip' else '⚠ 注意：'
    run = pg.add_run(prefix + text)
    run.font.size = Pt(10)
    run.font.bold = True if tip_type == 'warning' else False

def screenshot(doc, filename, caption="", width=14):
    path = os.path.join(SCREENSHOTS_DIR, filename)
    if os.path.exists(path):
        try:
            pg = doc.add_paragraph()
            pg.alignment = WD_ALIGN_PARAGRAPH.CENTER
            run = pg.add_run()
            run.add_picture(path, width=Cm(width))
            if caption:
                cp = doc.add_paragraph(caption)
                cp.alignment = WD_ALIGN_PARAGRAPH.CENTER
                for run in cp.runs:
                    run.font.size = Pt(9)
                    run.font.color.rgb = RGBColor(100, 100, 100)
        except Exception as e:
            doc.add_paragraph(f'[截图: {caption}]')
    else:
        doc.add_paragraph(f'[截图文件不存在: {filename}]')

def step_table(doc, steps):
    """Create step-by-step instruction table"""
    t = doc.add_table(rows=len(steps)+1, cols=3)
    set_table_borders(t)
    for j, h_text in enumerate(['步骤', '操作', '说明']):
        t.rows[0].cells[j].text = h_text
        set_cell_bg(t.rows[0].cells[j], '2E75B6')
        for pg in t.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
                run.font.size = Pt(10)
    for i, (num, op, desc) in enumerate(steps):
        t.rows[i+1].cells[0].text = str(num)
        t.rows[i+1].cells[1].text = op
        t.rows[i+1].cells[2].text = desc
        if i % 2 == 0:
            for cell in t.rows[i+1].cells:
                set_cell_bg(cell, 'F5F9FF')
        for pg in t.rows[i+1].cells[0].paragraphs:
            for run in pg.runs:
                run.font.bold = True
                run.font.size = Pt(10)
        for col in range(1, 3):
            for pg in t.rows[i+1].cells[col].paragraphs:
                for run in pg.runs:
                    run.font.size = Pt(10)
    return t

def create_doc3():
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
    sr = sp.add_run('用户使用手册')
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
        ('适用人员', '系统操作用户'),
    ]):
        it.rows[i].cells[0].text = k
        it.rows[i].cells[1].text = v
        set_cell_bg(it.rows[i].cells[0], 'D6E4F7')
    
    page_break(doc)
    
    # ===== TOC =====
    h(doc, '目录', 1)
    toc_items = [
        ('1', '系统概述', '3'),
        ('1.1', '系统简介', '3'),
        ('1.2', '浏览器要求', '3'),
        ('1.3', '系统访问地址', '3'),
        ('2', '用户登录', '4'),
        ('2.1', '普通登录', '4'),
        ('2.2', 'TOTP双因素认证登录', '4'),
        ('2.3', '首次登录修改密码', '4'),
        ('3', '项目管理', '5'),
        ('3.1', '项目列表查看', '5'),
        ('3.2', '项目搜索与过滤', '5'),
        ('3.3', '新建项目', '6'),
        ('3.4', '编辑项目', '8'),
        ('3.5', '查看项目详情', '8'),
        ('3.6', '项目统计', '9'),
        ('3.7', '项目导入/导出', '9'),
        ('4', '归档材料制作', '10'),
        ('4.1', '选择项目', '10'),
        ('4.2', '选择归档模板', '10'),
        ('4.3', '生成并下载归档材料', '11'),
        ('5', '基础数据管理（管理员）', '12'),
        ('5.1', '人员管理', '12'),
        ('5.2', '测评设备管理', '13'),
        ('5.3', '归档模板管理', '13'),
        ('5.4', '字典管理', '14'),
        ('6', '系统管理（管理员）', '15'),
        ('6.1', '用户管理', '15'),
        ('6.2', '系统配置', '16'),
        ('7', '日志管理（管理员）', '17'),
        ('7.1', '登录日志', '17'),
        ('7.2', '操作日志', '17'),
        ('8', '个人账户管理', '18'),
        ('8.1', '修改密码', '18'),
        ('8.2', '启用/关闭TOTP认证', '18'),
    ]
    for num, title, pg_num in toc_items:
        pg = doc.add_paragraph()
        indent = Cm(0.5 if '.' in num and num.count('.') == 1 else (1.0 if num.count('.') > 1 else 0))
        pg.paragraph_format.left_indent = indent
        r1 = pg.add_run(f'{num}  {title}')
        r1.font.size = Pt(11)
        r2 = pg.add_run(f'    ...  {pg_num}')
        r2.font.size = Pt(11)
    
    page_break(doc)
    
    # ===== CHAPTER 1: OVERVIEW =====
    h(doc, '1  系统概述', 1)
    
    h(doc, '1.1  系统简介', 2)
    tx(doc, '等保项目管理及归档管理系统（v2.0）是南京国云电力有限公司内部使用的等级保护测评项目管理平台，主要功能包括：')
    items = [
        '管理等保测评项目全流程信息（立项→配置人员→被测系统→归档）；',
        '自动生成标准化归档材料（保密协议、任务书、计划书、报告等），一键导出ZIP压缩包；',
        '查看项目统计分析报表（按经理、类型、行业等维度统计）；',
        '管理测评人员、硬件设备和软件工具台账；',
        '完整的操作日志审计，保障数据安全。',
    ]
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    
    h(doc, '1.2  浏览器要求', 2)
    tx(doc, '推荐使用以下浏览器访问系统，以获得最佳体验：')
    browser_data = [
        ('Google Chrome', '80+', '推荐，体验最佳'),
        ('Microsoft Edge', '80+', '推荐'),
        ('Mozilla Firefox', '75+', '支持'),
        ('Safari', '13+', 'macOS用户可使用'),
    ]
    bt = doc.add_table(rows=len(browser_data)+1, cols=3)
    set_table_borders(bt)
    for j, hd in enumerate(['浏览器', '最低版本', '说明']):
        bt.rows[0].cells[j].text = hd
        set_cell_bg(bt.rows[0].cells[j], '2E75B6')
        for pg in bt.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (a, b, c) in enumerate(browser_data):
        bt.rows[i+1].cells[0].text = a
        bt.rows[i+1].cells[1].text = b
        bt.rows[i+1].cells[2].text = c
    
    h(doc, '1.3  系统访问地址', 2)
    tx(doc, '系统访问地址：https://njgy.dbcp.cn')
    tip(doc, '首次访问时，浏览器可能提示"您的连接不是私密连接"（因使用自签名SSL证书），请点击"高级"→"继续访问"。')
    
    page_break(doc)
    
    # ===== CHAPTER 2: LOGIN =====
    h(doc, '2  用户登录', 1)
    
    h(doc, '2.1  普通登录', 2)
    screenshot(doc, '01_login.png', '图2-1 系统登录界面', 13)
    doc.add_paragraph()
    step_table(doc, [
        ('1', '打开浏览器，访问 https://njgy.dbcp.cn', '显示系统登录页面'),
        ('2', '在"用户名"输入框填写登录账号', '由管理员分配，通常为工号或姓名缩写'),
        ('3', '在"密码"输入框填写密码', '首次登录使用初始密码，登录后需立即修改'),
        ('4', '在"验证码"输入框填写图形验证码', '点击验证码图片可刷新'),
        ('5', '点击"登录"按钮', '验证通过后进入系统主界面'),
    ])
    doc.add_paragraph()
    tip(doc, '如连续5次输入错误密码，账号将被锁定，请联系系统管理员解锁。')
    
    h(doc, '2.2  TOTP双因素认证登录', 2)
    tx(doc, '如果账号已开启TOTP双因素认证，在完成步骤1-5的普通登录后，还需要额外完成以下步骤：')
    step_table(doc, [
        ('1', '打开Google Authenticator（或其他TOTP认证App）', '手机端安装，由管理员引导绑定'),
        ('2', '查看当前6位动态验证码', '验证码每30秒更新一次'),
        ('3', '在系统弹出的TOTP输入框中填写验证码', ''),
        ('4', '点击确认', '验证成功后进入系统'),
    ])
    doc.add_paragraph()
    tip(doc, 'TOTP验证码每30秒刷新一次，请在验证码过期前及时输入。', 'warning')
    
    h(doc, '2.3  首次登录修改密码', 2)
    tx(doc, '新账号首次登录时，系统会强制要求修改密码：')
    step_table(doc, [
        ('1', '登录后系统弹出修改密码对话框', '或跳转到修改密码页面'),
        ('2', '输入当前密码（初始密码）', ''),
        ('3', '输入新密码', '要求：8位以上，包含大小写字母和数字'),
        ('4', '再次输入新密码确认', '两次输入必须一致'),
        ('5', '点击"保存"按钮', '密码修改成功后重新登录'),
    ])
    
    page_break(doc)
    
    # ===== CHAPTER 3: PROJECT MANAGEMENT =====
    h(doc, '3  项目管理', 1)
    
    h(doc, '3.1  项目列表查看', 2)
    tx(doc, '登录后进入系统主界面，点击左侧菜单"项目管理 → 项目总览"进入项目列表页面。')
    screenshot(doc, '03_project_list.png', '图3-1 项目总览列表', 14)
    doc.add_paragraph()
    tx(doc, '项目列表页展示以下信息：')
    items = [
        '项目编号：唯一标识，如"CP26-001-001"；',
        '项目名称：加密存储，界面显示明文；',
        '委托单位：加密存储，界面显示明文；',
        '项目经理：负责该项目的经理姓名；',
        '项目组成员：登记测评师名单（多人以顿号分隔）；',
        '系统数量：该项目下的被测系统总数（含二级/三级分类）；',
        '归档状态：纸质归档、电子归档状态标签；',
        '操作按钮：详情、编辑、删除。',
    ]
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    
    h(doc, '3.2  项目搜索与过滤', 2)
    
    h(doc, '3.2.1  快速搜索', 3)
    tx(doc, '在列表顶部的搜索栏中，可以快速按以下条件筛选：')
    items = ['项目编号（精确或模糊匹配）', '项目名称（模糊匹配）', '合同签署日期范围']
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    
    h(doc, '3.2.2  高级搜索', 3)
    tx(doc, '点击搜索栏右侧的"高级搜索"按钮，展开高级搜索面板：')
    screenshot(doc, '04_project_advanced_search.png', '图3-2 高级搜索面板', 14)
    doc.add_paragraph()
    adv_data = [
        ('备案编号', '按被测系统的备案编号搜索（子表字段，支持模糊匹配）'),
        ('委托单位', '按委托单位名称模糊搜索'),
        ('项目负责人', '按项目负责人姓名模糊搜索'),
        ('项目经理', '从下拉列表选择项目经理'),
        ('项目组成员', '按登记测评师姓名模糊搜索，显示该人员参与的所有项目'),
        ('项目类型', '从字典选择项目类型（如：电力行业项目）'),
        ('所属行业', '从字典选择所属行业'),
        ('所属年份', '选择项目所属年份进行过滤'),
    ]
    adt = doc.add_table(rows=len(adv_data)+1, cols=2)
    set_table_borders(adt)
    for j, hd in enumerate(['搜索字段', '说明']):
        adt.rows[0].cells[j].text = hd
        set_cell_bg(adt.rows[0].cells[j], '2E75B6')
        for pg in adt.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (k, v) in enumerate(adv_data):
        adt.rows[i+1].cells[0].text = k
        adt.rows[i+1].cells[1].text = v
    
    h(doc, '3.3  新建项目', 2)
    tx(doc, '点击项目列表页右上角"新建"按钮，进入新建项目表单页面。')
    doc.add_paragraph()
    tx(doc, '项目表单分为以下几个信息区域：')
    
    h(doc, '【基本信息】', 3)
    basic_data = [
        ('项目编号', '必填，唯一标识，如"CP26-001-001"'),
        ('项目名称', '必填，项目的完整名称（加密存储）'),
        ('委托单位', '必填，客户单位名称（加密存储）'),
        ('客户地址', '选填，委托单位地址'),
        ('联系人', '选填，客户联系人姓名'),
        ('联系电话', '选填，客户联系电话'),
        ('项目类型', '从字典下拉选择项目类型'),
        ('所属行业', '从字典下拉选择行业'),
        ('项目经理', '从人员列表选择项目经理'),
        ('项目负责人', '从人员列表选择项目负责人'),
        ('合同日期', '选填，合同签署日期'),
        ('合同金额', '选填（加密存储）'),
    ]
    bt2 = doc.add_table(rows=len(basic_data)+1, cols=2)
    set_table_borders(bt2)
    for j, hd in enumerate(['字段名称', '说明']):
        bt2.rows[0].cells[j].text = hd
        set_cell_bg(bt2.rows[0].cells[j], '2E75B6')
        for pg in bt2.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (k, v) in enumerate(basic_data):
        bt2.rows[i+1].cells[0].text = k
        bt2.rows[i+1].cells[1].text = v
    
    h(doc, '【被测系统配置】', 3)
    tx(doc, '点击"添加系统"按钮可以添加多个被测系统，每个系统需填写：')
    items = ['系统名称（必填，加密存储）', '安全等级（二级/三级，必填）', '测评指标（选填）', '备案编号（选填）']
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    tip(doc, '一个项目可以包含多个被测系统（如同时有二级系统和三级系统），点击"+ 添加系统"按钮可继续添加。')
    
    h(doc, '【项目人员配置】', 3)
    tx(doc, '按角色为项目配置相关人员，支持配置以下12种角色：')
    role_data = [
        ('登记测评师', '在项目总览中显示为"项目组成员"，是项目的核心参与人员'),
        ('实际参与人员', '实际参与测评工作的人员'),
        ('调研访谈编制人', '负责编写调研访谈记录的人员'),
        ('测评方案编制人', '负责编写测评方案的人员'),
        ('测评报告编制人', '负责编写测评报告的人员'),
        ('网络测评人', '负责网络安全测评的人员'),
        ('主机测评人', '负责主机安全测评的人员'),
        ('物理测评人', '负责物理安全测评的人员'),
        ('工具扫描人', '负责使用工具扫描的人员'),
        ('渗透测试人', '负责渗透测试的人员'),
    ]
    rt = doc.add_table(rows=len(role_data)+1, cols=2)
    set_table_borders(rt)
    for j, hd in enumerate(['角色', '说明']):
        rt.rows[0].cells[j].text = hd
        set_cell_bg(rt.rows[0].cells[j], '2E75B6')
        for pg in rt.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (k, v) in enumerate(role_data):
        rt.rows[i+1].cells[0].text = k
        rt.rows[i+1].cells[1].text = v
    
    h(doc, '【其他信息】', 3)
    tx(doc, '包括：纸质归档/电子归档状态、各阶段完成时间（准备→计划→现场→报告）、任务书签约日期、所属年份、业务人员、项目地区、报告邮寄日期、报告邮寄编号、备注。')
    
    tx(doc, '\n表单填写完毕后，点击页面底部的"保存"按钮创建项目；点击"取消"放弃创建。')
    
    h(doc, '3.4  编辑项目', 2)
    tx(doc, '在项目列表中，点击项目行右侧的"编辑"按钮，进入编辑表单页面。编辑表单与新建表单相同，修改后点击"保存"即可更新项目信息。')
    tip(doc, '项目编号不支持修改，如需变更项目编号需删除重建。')
    
    h(doc, '3.5  查看项目详情', 2)
    tx(doc, '点击项目行右侧的"详情"按钮，进入项目详情页面，可查看项目的完整信息。')
    screenshot(doc, '05_project_detail.png', '图3-3 项目详情页面', 14)
    doc.add_paragraph()
    tx(doc, '项目详情页按信息类别分组展示，包含：基本信息、被测系统列表、项目人员（按角色分类）、归档状态、进度追踪、其他信息等完整内容。')
    
    h(doc, '3.6  项目统计', 2)
    tx(doc, '点击左侧菜单"项目管理 → 项目统计"进入统计图表页面。')
    screenshot(doc, '07_project_stats.png', '图3-4 项目统计图表', 14)
    doc.add_paragraph()
    tx(doc, '统计图表提供以下维度的可视化分析：')
    items = [
        '各项目经理负责的项目数量（柱状图）；',
        '各项目类型的数量分布（饼图）；',
        '各行业项目的数量分布（饼图）；',
        '二级/三级系统数量统计（柱状图）。',
    ]
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    
    h(doc, '3.7  项目导入/导出', 2)
    tx(doc, '系统支持通过Excel文件批量导入项目数据，以及将当前查询结果导出为Excel文件。')
    
    tx(doc, '批量导入步骤：', bold=True)
    step_table(doc, [
        ('1', '点击项目列表页右上角"导入"按钮', ''),
        ('2', '点击"下载模板"下载标准Excel模板', '按照模板格式填写项目数据'),
        ('3', '填写完成后点击"选择文件"上传', ''),
        ('4', '点击"确认导入"', '系统自动处理，显示导入结果'),
    ])
    doc.add_paragraph()
    tx(doc, '导出操作：', bold=True)
    tx(doc, '点击项目列表页右上角"导出"按钮，系统将当前查询结果（含所有过滤条件）导出为Excel文件下载。')
    
    page_break(doc)
    
    # ===== CHAPTER 4: ARCHIVE =====
    h(doc, '4  归档材料制作', 1)
    tx(doc, '归档材料制作功能可根据项目信息自动生成标准化的归档文档，支持批量生成并打包下载。')
    tx(doc, '点击左侧菜单"归档管理 → 归档材料制作"进入操作页面。')
    screenshot(doc, '08_archive_page.png', '图4-1 归档材料制作页面', 14)
    
    h(doc, '4.1  选择项目', 2)
    step_table(doc, [
        ('1', '在"关键词"搜索框输入项目编号或项目名称', '支持模糊匹配，如输入"CP26"可显示所有CP26开头的项目'),
        ('2', '选择所属年份（可选）', '可按年份过滤项目'),
        ('3', '点击"查询"按钮', '下方显示符合条件的项目列表'),
        ('4', '点击目标项目所在行', '选中的项目高亮显示，右侧显示项目编号和名称确认信息'),
    ])
    
    h(doc, '4.2  选择归档模板', 2)
    tx(doc, '在项目列表下方，系统展示所有可用的归档模板列表。')
    step_table(doc, [
        ('1', '勾选需要生成的模板文档', '可多选，勾选所有常用模板'),
        ('2', '查看模板名称确认选择', '如：保密协议、任务书、测评方案等'),
    ])
    doc.add_paragraph()
    tip(doc, '不同项目类型和系统等级对应不同的归档模板，请根据实际项目需求选择合适的模板。')
    
    h(doc, '4.3  生成并下载归档材料', 2)
    step_table(doc, [
        ('1', '确认已选择项目和模板后，点击"生成归档材料"按钮', '系统开始自动填充模板中的占位符'),
        ('2', '等待系统生成完成', '根据模板数量和复杂度，通常需要10-30秒'),
        ('3', '生成完成后页面显示成功提示和文件列表', ''),
        ('4', '点击"下载"按钮', '系统将所有生成的文档打包为ZIP文件下载'),
        ('5', '下载完成后解压ZIP文件查看', 'ZIP文件以"项目编号-归档材料.zip"命名'),
    ])
    doc.add_paragraph()
    tip(doc, '生成的Word文档中，所有项目信息（项目名称、委托单位、人员名单等）已自动填充完毕，可直接打印或进行最终审核修改。')
    tip(doc, '若生成过程中报错，请检查：1）项目信息是否填写完整；2）所选模板文件是否有效；3）联系管理员查看系统日志。', 'warning')
    
    page_break(doc)
    
    # ===== CHAPTER 5: BASIC DATA MANAGEMENT =====
    h(doc, '5  基础数据管理（管理员）', 1)
    tx(doc, '以下功能需要管理员权限，普通用户无法访问。')
    
    h(doc, '5.1  人员管理', 2)
    tx(doc, '点击左侧菜单"基础数据 → 人员管理"进入人员管理页面。')
    screenshot(doc, '09_staff_manage.png', '图5-1 人员管理界面', 13)
    doc.add_paragraph()
    
    h(doc, '添加人员', 3)
    step_table(doc, [
        ('1', '点击右上角"新增"按钮', '弹出新增人员表单'),
        ('2', '填写人员编号、姓名、部门、职位', '姓名为必填项'),
        ('3', '选择资质等级（高级/中级/初级）', ''),
        ('4', '填写证书编号、证书有效期', '可选'),
        ('5', '填写联系电话、邮箱', '均加密存储'),
        ('6', '点击"确定"保存', ''),
    ])
    
    h(doc, '编辑/停用人员', 3)
    tx(doc, '在人员列表中，点击"编辑"按钮修改人员信息；点击"停用"按钮将人员状态设为停用（不影响已关联的项目数据）。')
    
    h(doc, '批量导入', 3)
    step_table(doc, [
        ('1', '点击"导入"按钮', ''),
        ('2', '下载并填写Excel模板', '按模板格式填写人员信息'),
        ('3', '上传填写好的Excel文件', ''),
        ('4', '点击"导入"确认', '成功/失败统计将显示在弹窗中'),
    ])
    
    h(doc, '5.2  测评设备管理', 2)
    tx(doc, '点击左侧菜单"基础数据 → 设备管理"进入测评设备管理页面。')
    screenshot(doc, '10_device_manage.png', '图5-2 测评设备管理界面', 13)
    doc.add_paragraph()
    tx(doc, '测评设备包括：')
    items = ['测评专用机（device_type=1）：测评工作使用的专用计算机', '扫描设备（device_type=2）：用于漏洞扫描的硬件设备，可关联归属人员']
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    tip(doc, '扫描设备需要关联归属人员，在生成归档材料的"工具清单"时，系统会自动匹配项目成员的扫描设备。')
    
    h(doc, '5.3  归档模板管理', 2)
    tx(doc, '点击左侧菜单"基础数据 → 模板管理"进入归档模板管理页面。')
    screenshot(doc, '11_template_manage.png', '图5-3 归档模板管理界面', 13)
    doc.add_paragraph()
    
    h(doc, '上传新模板', 3)
    step_table(doc, [
        ('1', '点击"上传模板"按钮', ''),
        ('2', '填写模板编码、模板名称', '模板编码需唯一'),
        ('3', '选择模板分类、适用项目类型、适用等级', ''),
        ('4', '填写版本号', '如 v1.0'),
        ('5', '点击上传区域选择Word文件（.docx）', '只支持.doc/.docx格式'),
        ('6', '点击"确定"提交', ''),
    ])
    
    h(doc, '替换模板文件', 3)
    tx(doc, '在模板列表中，点击"替换"按钮可以上传新版本的模板文件，原有模板配置（编码、名称等）保持不变，只更新文件内容。')
    
    h(doc, '模板占位符说明', 3)
    tx(doc, '在设计Word模板时，使用以下格式的占位符，系统在生成时会自动替换为实际数据：')
    ph_data = [
        ('{{xmbh}}', '项目编号', '如：CP26-001-001'),
        ('{{xmmc}}', '项目名称', ''),
        ('{{khmc}}', '委托单位名称', ''),
        ('{{xtmc}}', '被测系统名称', '多个系统用顿号分隔'),
        ('{{xmfzr}}', '项目负责人', ''),
        ('{{xmjl}}', '项目经理', ''),
        ('{{djcps}}', '登记测评师', '多人用顿号分隔'),
        ('{{bah1}}...{{bahN}}', '各系统备案编号', '按系统序号1,2,3...'),
        ('{{contractDate}}', '合同日期', ''),
    ]
    pht = doc.add_table(rows=len(ph_data)+1, cols=3)
    set_table_borders(pht)
    for j, hd in enumerate(['占位符', '对应信息', '备注']):
        pht.rows[0].cells[j].text = hd
        set_cell_bg(pht.rows[0].cells[j], '2E75B6')
        for pg in pht.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (a, b, c) in enumerate(ph_data):
        pht.rows[i+1].cells[0].text = a
        pht.rows[i+1].cells[1].text = b
        pht.rows[i+1].cells[2].text = c
    
    h(doc, '5.4  字典管理', 2)
    tx(doc, '点击左侧菜单"基础数据 → 字典管理"进入字典管理页面。')
    screenshot(doc, '13_dict_manage.png', '图5-4 字典管理界面', 13)
    doc.add_paragraph()
    tx(doc, '字典管理用于维护系统中各类下拉选项的数据，如项目类型、所属行业、资质等级等。')
    step_table(doc, [
        ('1', '在左侧选择字典分类（如：项目类型）', ''),
        ('2', '在右侧查看/添加/编辑字典项', ''),
        ('3', '点击"添加"输入字典项名称和排序值', ''),
        ('4', '点击"保存"', '修改立即生效，在新建/编辑项目时下拉框中可见'),
    ])
    
    page_break(doc)
    
    # ===== CHAPTER 6: SYSTEM MANAGEMENT =====
    h(doc, '6  系统管理（管理员）', 1)
    
    h(doc, '6.1  用户管理', 2)
    tx(doc, '点击左侧菜单"系统管理 → 用户管理"进入用户管理页面。')
    screenshot(doc, '12_user_manage.png', '图6-1 用户管理界面', 13)
    doc.add_paragraph()
    
    h(doc, '创建新用户', 3)
    step_table(doc, [
        ('1', '点击"新增用户"按钮', ''),
        ('2', '填写用户名（登录账号）', '用户名唯一，不可重复'),
        ('3', '填写真实姓名', '加密存储'),
        ('4', '设置初始密码', '用户首次登录后需修改'),
        ('5', '选择角色（系统管理员/普通用户等）', '角色决定权限范围'),
        ('6', '填写手机号、邮箱（可选）', ''),
        ('7', '点击"确定"创建', ''),
    ])
    
    h(doc, '重置用户密码', 3)
    tx(doc, '在用户列表中，点击"重置密码"按钮，为该用户设置新的临时密码，用户下次登录时需修改密码。')
    
    h(doc, '启用/停用用户', 3)
    tx(doc, '点击用户列表中的"启用"/"停用"按钮，管理用户的登录权限。停用的用户无法登录系统。')
    
    h(doc, 'TOTP认证管理', 3)
    tx(doc, '管理员可以在用户管理中查看用户的TOTP状态（已开启/未开启），并可以重置用户的TOTP绑定（用户下次登录时需重新扫码绑定）。')
    
    h(doc, '6.2  系统配置', 2)
    tx(doc, '点击左侧菜单"系统管理 → 系统配置"进入系统配置页面。')
    screenshot(doc, '14_system_config.png', '图6-2 系统配置界面', 13)
    doc.add_paragraph()
    
    config_data = [
        ('系统名称（SYS_NAME）', '修改系统登录页和标题栏显示的名称', '修改后立即生效'),
        ('公司LOGO（SYS_LOGO）', '上传公司Logo图片，显示在左上角', '建议PNG格式，200×60像素以内'),
    ]
    ct = doc.add_table(rows=len(config_data)+1, cols=3)
    set_table_borders(ct)
    for j, hd in enumerate(['配置项', '说明', '备注']):
        ct.rows[0].cells[j].text = hd
        set_cell_bg(ct.rows[0].cells[j], '2E75B6')
        for pg in ct.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (a, b, c) in enumerate(config_data):
        ct.rows[i+1].cells[0].text = a
        ct.rows[i+1].cells[1].text = b
        ct.rows[i+1].cells[2].text = c
    
    page_break(doc)
    
    # ===== CHAPTER 7: LOG MANAGEMENT =====
    h(doc, '7  日志管理（管理员）', 1)
    
    h(doc, '7.1  登录日志', 2)
    tx(doc, '点击左侧菜单"日志管理 → 登录日志"，查看所有用户的登录记录。')
    screenshot(doc, '15_login_log.png', '图7-1 登录日志界面', 13)
    doc.add_paragraph()
    tx(doc, '登录日志记录以下信息：')
    items = ['登录用户名和真实姓名', '登录IP地址', '登录时间', '登录状态（成功/失败）', '失败原因（如：密码错误、账号锁定等）']
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    tx(doc, '支持按用户名、登录状态、时间范围进行查询过滤。')
    
    h(doc, '7.2  操作日志', 2)
    tx(doc, '点击左侧菜单"日志管理 → 操作日志"，查看系统所有关键操作记录。')
    screenshot(doc, '16_operation_log.png', '图7-2 操作日志界面', 13)
    doc.add_paragraph()
    tx(doc, '操作日志记录以下信息：')
    items = ['操作用户', '操作类型（CREATE/UPDATE/DELETE/EXPORT等）', '操作对象（项目/人员/设备等）', '操作详细描述', '操作IP地址', '操作时间']
    for item in items:
        pg = doc.add_paragraph(style='List Bullet')
        pg.add_run(item).font.size = Pt(11)
    tx(doc, '支持按用户、操作类型、时间范围进行查询过滤。')
    tip(doc, '操作日志是系统审计的重要依据，建议定期导出备份并归档保存。')
    
    page_break(doc)
    
    # ===== CHAPTER 8: PERSONAL ACCOUNT =====
    h(doc, '8  个人账户管理', 1)
    
    h(doc, '8.1  修改密码', 2)
    tx(doc, '点击右上角头像/用户名 → "修改密码"，进入密码修改页面。')
    step_table(doc, [
        ('1', '输入当前密码', ''),
        ('2', '输入新密码', '要求8位以上，包含大小写字母和数字'),
        ('3', '再次输入新密码确认', '两次必须一致'),
        ('4', '点击"保存"', '修改成功后系统提示重新登录'),
    ])
    doc.add_paragraph()
    tip(doc, '建议定期修改密码（每3-6个月），密码应包含大小写字母、数字，长度不少于8位。', 'warning')
    
    h(doc, '8.2  启用/关闭TOTP认证', 2)
    tx(doc, '点击右上角头像/用户名 → "安全设置"，管理双因素认证设置。')
    
    h(doc, '启用TOTP', 3)
    step_table(doc, [
        ('1', '点击"启用TOTP认证"', '系统显示二维码'),
        ('2', '使用Google Authenticator扫描二维码', '手机安装Google Authenticator或同类App'),
        ('3', '输入App显示的6位验证码确认绑定', ''),
        ('4', '点击"确认绑定"', '下次登录时将要求输入TOTP验证码'),
    ])
    
    h(doc, '关闭TOTP', 3)
    step_table(doc, [
        ('1', '点击"关闭TOTP认证"', ''),
        ('2', '输入当前TOTP验证码确认身份', ''),
        ('3', '点击"确认关闭"', '下次登录不再需要TOTP验证码'),
    ])
    doc.add_paragraph()
    tip(doc, '开启TOTP双因素认证可以显著提升账号安全性，建议所有用户特别是管理员账号开启此功能。')
    
    # ===== FINAL PAGE: QUICK REFERENCE =====
    page_break(doc)
    h(doc, '附录：常见操作快速参考', 1)
    quick_ref = [
        ('新建项目', '项目管理 → 项目总览 → 点击"新建"按钮'),
        ('按备案编号搜索', '项目总览 → 高级搜索 → 填写"备案编号"'),
        ('查找某人参与的所有项目', '项目总览 → 高级搜索 → 填写"项目组成员"（登记测评师）'),
        ('生成归档材料', '归档管理 → 归档材料制作 → 选择项目 → 选择模板 → 生成'),
        ('添加新人员', '基础数据 → 人员管理 → 点击"新增"'),
        ('上传归档模板', '基础数据 → 模板管理 → 点击"上传模板"'),
        ('查看登录异常', '日志管理 → 登录日志 → 筛选"失败"状态'),
        ('修改密码', '右上角用户名 → 修改密码'),
        ('统计各经理项目', '项目管理 → 项目统计 → 查看图表'),
        ('批量导入项目', '项目管理 → 项目总览 → 点击"导入"'),
    ]
    qrt = doc.add_table(rows=len(quick_ref)+1, cols=2)
    set_table_borders(qrt)
    for j, hd in enumerate(['操作目标', '操作路径']):
        qrt.rows[0].cells[j].text = hd
        set_cell_bg(qrt.rows[0].cells[j], '1F3864')
        for pg in qrt.rows[0].cells[j].paragraphs:
            for run in pg.runs:
                run.font.color.rgb = RGBColor(255,255,255)
                run.font.bold = True
    for i, (k, v) in enumerate(quick_ref):
        qrt.rows[i+1].cells[0].text = k
        qrt.rows[i+1].cells[1].text = v
        if i % 2 == 0:
            for cell in qrt.rows[i+1].cells:
                set_cell_bg(cell, 'EBF3FB')
    
    # Save
    output_path = os.path.join(OUTPUT_DIR, '文档3-用户使用手册.docx')
    doc.save(output_path)
    print(f"Doc3 saved: {output_path}")
    print(f"File size: {os.path.getsize(output_path)} bytes")

create_doc3()
