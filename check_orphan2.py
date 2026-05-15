# -*- coding: utf-8 -*-
import pymysql

conn = pymysql.connect(host='localhost', user='root', password='zxcv1234', database='ry-vue', charset='utf8mb4')
cur = conn.cursor()

print("=== 1. 孤儿 field（table_id 在 bitable_table 中不存在）===")
cur.execute("""
    SELECT f.id, f.field_name, f.table_id, f.app_token
    FROM bitable_field f
    LEFT JOIN bitable_table t ON f.table_id = t.table_id AND f.app_token = t.app_token
    WHERE t.id IS NULL
""")
orphan_fields = cur.fetchall()
if orphan_fields:
    for r in orphan_fields:
        print(r)
    print(f"共 {len(orphan_fields)} 条孤儿 field")
else:
    print("无孤儿 field")

print("\n=== 2. 孤儿 record（table_id 在 bitable_table 中不存在）===")
cur.execute("""
    SELECT r.id, r.record_id, r.table_id, r.app_token
    FROM bitable_record r
    LEFT JOIN bitable_table t ON r.table_id = t.table_id AND r.app_token = t.app_token
    WHERE t.id IS NULL
""")
orphan_records = cur.fetchall()
if orphan_records:
    for r in orphan_records:
        print(r)
    print(f"共 {len(orphan_records)} 条孤儿 record")
else:
    print("无孤儿 record")

print("\n=== 3. 孤儿 table（app_id 在 bitable_app 中不存在）===")
cur.execute("""
    SELECT t.id, t.table_name, t.app_id
    FROM bitable_table t
    LEFT JOIN bitable_app a ON t.app_id = a.id
    WHERE a.id IS NULL
""")
orphan_tables = cur.fetchall()
if orphan_tables:
    for r in orphan_tables:
        print(r)
    print(f"共 {len(orphan_tables)} 张孤儿 table")
else:
    print("无孤儿 table")

print("\n=== 4. 当前数据统计 ===")
cur.execute("SELECT COUNT(*) FROM bitable_app")
print(f"应用: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM bitable_table")
print(f"数据表: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM bitable_field")
print(f"字段: {cur.fetchone()[0]}")
cur.execute("SELECT COUNT(*) FROM bitable_record")
print(f"记录: {cur.fetchone()[0]}")

conn.close()
