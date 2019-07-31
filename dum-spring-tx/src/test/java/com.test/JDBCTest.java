package com.test;

import com.dum.spring.transaction.entity.Member;
import com.dum.spring.transaction.entity.Order;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class JDBCTest {
    @Test
    public void client() {
        Order condition = new Order();
        condition.setId((long) 1);
        List<Object> results = select(condition);
        System.out.println(Arrays.toString(results.toArray()));
    }

    private List<Object> select(Object entity) {
        Class entityClass = entity.getClass();
        List<Object> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            // 1.加载驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 2.建立连接
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DUM_ORM", "root", "123456");
            // 3.创建语句集
            Table table = (Table) entityClass.getAnnotation(Table.class);
            String sql = "select * from " + table.name();

            Map<String, String> mapper = new HashMap<>();
            Map<String, String> mapFieldNameColumn = new HashMap<>();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    mapper.put(field.getAnnotation(Column.class).name(), field.getName());
                    mapFieldNameColumn.put(field.getName(), field.getAnnotation(Column.class).name());
                }
                mapper.put(field.getName(), field.getName());
                mapFieldNameColumn.put(field.getName(), field.getName());
            }
            // 匹配传进来对象的属性值
            StringBuffer where = new StringBuffer(" where 1=1");
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (null != value)
                {
                    if (String.class == field.getType()) {
                        where.append(" and " + mapFieldNameColumn.get(field.getName()) + " = '" + value + "'");
                    }else {
                        where.append(" and " + mapFieldNameColumn.get(field.getName()) + " = " + value);
                    }// 其他类型暂时不考虑
                }
            }
            sql += where;
            pstm = conn.prepareStatement(sql);
            // 4.执行语句集
            rs = pstm.executeQuery();
            // 5.输出结果
            while (rs.next()) {
                Object object = entityClass.newInstance();
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    Field field = entityClass.getDeclaredField(mapper.get(columnName));
                    field.setAccessible(true);
                    field.set(object, rs.getObject(columnName));
                }
                results.add(object);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                pstm.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return results;
    }


}
