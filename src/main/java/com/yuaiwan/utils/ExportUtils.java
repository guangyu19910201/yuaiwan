package com.yuaiwan.utils;

import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.IndexedColors;


/**
 * 该工具类用于批量导出
 * @author guangyu
 */
public class ExportUtils {
	
	public static void export(String databaseName,String filename){
		
	}
	
	public static String TableStructInfoToExcel(List list,String path) throws Exception {
        String FileName="";
        FileOutputStream fos = null;
        HSSFRow row = null;
        HSSFCell cell = null;
        HSSFCellStyle style = null;
        HSSFCellStyle style1 = null;
        HSSFFont font = null;
        int currentRowNum = 0;
        String[] tableFiled = {"no","column_name","data_type","data_length","data_precision","data_Scale","nullable","data_default","comments"};
        String[] tableFiled_ch = {"序号","字段名","字段类型","字段长度","data_precision","data_Scale","是否为空","默认值","备注"};
        try{
            FileName = "D:\\"+"CSN数据库中表结构"+System.currentTimeMillis()+".xls";
            fos = new FileOutputStream(FileName);
            //创建新的sheet并设置名称
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet s = wb.createSheet();
            wb.setSheetName(0, "CSN数据库表结构");
            style = wb.createCellStyle();
            style1 = wb.createCellStyle();
            font = wb.createFont();
            for(int z=0;z<list.size();z++){
                List listBean = (List) list.get(z);
                //新建一行,再在行上面新建一列
                row = s.createRow(currentRowNum);
                int pad = currentRowNum;
                currentRowNum++;
                //设置样式
                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);   //字体加粗
                style.setFont(font);
                style.setAlignment(HSSFCellStyle.ALIGN_LEFT);//水平居中
                style.setFillForegroundColor((short) 4);// 设置背景色
                style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
                style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                style1.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
                style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
                style1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
                style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
                style1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
                style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
                style1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
                for(int i=0;i<tableFiled.length;i++){
                    cell = row.createCell(i);
                    cell.setCellValue("");
                    cell.setCellStyle(style);
                }
                row.getCell(0).setCellValue("表名："+listBean.get(0).toString());
                //创建第二行
                row = s.createRow(currentRowNum);
                currentRowNum++;
                for(int i=0;i<tableFiled.length;i++){
                	//创建多列并设置每一列的值和宽度
                    cell = row.createCell(i);
                    cell.setCellValue(new HSSFRichTextString(tableFiled_ch[i]));
                    cell.setCellStyle(style1);
                    s.setColumnWidth(i,5000);
                }
                List list2 = (List) listBean.get(1);
                for(int i=0;i<list2.size();i++){
                    row = s.createRow(currentRowNum);
                    currentRowNum++;
                    String[] strings = (String[]) list2.get(i);
                    System.out.println("");
                    for(int j=0;j<strings.length;j++){
                        System.out.print(strings[j]);
                        cell = row.createCell(j);
                        cell.setCellValue(new HSSFRichTextString(strings[j]));
                        cell.setCellStyle(style1);
                    }
                }
                //合并单元格
                s.addMergedRegion(new Region(pad,(short)0,pad,(short)(tableFiled.length-1)));
                currentRowNum ++;
            }
            wb.write(fos);
            fos.close();
        }catch (Exception e) {
            e.printStackTrace();
            fos.close();
            throw new Exception(FileName);
        }
        return FileName;
    }
}
