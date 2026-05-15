# -*- coding: utf-8 -*-
import pymysql

conn = pymysql.connect(host='localhost', user='root', password='zxcv1234', database='ry-vue', charset='utf8mb4')
cur = conn.cursor()

# 先查看 bitable_table 表结构
cur.execute("DESCRIBE bitable_table")
print("=== bitable_table 表结构 ===")
for row in cur.fetchall():
    print(row[0], '|', row[1])

print()

# 孤儿table（app已不存在但table还在）
cur.execute("""
    SELECT t.id, t.app_id, t.del_flag
    FROM bitable_table t
    LEFT JOIN bitable_app a ON t.app_id = a.id
    WHERE a.id IS NULL
""")
orphan_tables = cur.fetchall()

print("=== 孤儿 table ===")
if orphan_tables:
    for r in orphan_tables:
        print(r)
else:
    print("无")

# 统计
cur.execute("SELECT COUNT(*) FROM bitable_app WHERE del_flag='0'")
print("应用总数:", cur.fetchone()[0])

cur.execute("SELECT COUNT(*) FROM bitable_table WHERE del_flag='0'")
print("数据表总数:", cur.fetchone()[0])

conn.close()
