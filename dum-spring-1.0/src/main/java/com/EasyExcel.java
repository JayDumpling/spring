package com;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @Auther : Dumpling
 * @Description
 **/
public class EasyExcel {
    public static void main(String[] args) {
        try {
            OutputStream out = new FileOutputStream("D:\\withoutHead.xlsx");
            ExcelWriter excelWriter = new ExcelWriter(out, ExcelTypeEnum.XLSX, false);
            Sheet sheet1 = new Sheet(1, 0);
            sheet1.setSheetName("sheet1");
            List<List<String>> data = new ArrayList<>();
            for (int i = 0; i < 100; i++)
            {
                List<String> item = new ArrayList<>();
                item.add("item0" + i); item.add("item1" + i);
                item.add("item2" + i); data.add(item);
            }
            excelWriter.write0(data, sheet1);
            excelWriter.finish();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
