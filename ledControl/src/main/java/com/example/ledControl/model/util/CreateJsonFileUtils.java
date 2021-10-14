package com.example.ledControl.model.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * 生成JSON文件
 * 
 * @author Dhjie
 *
 */
public class CreateJsonFileUtils {

	/**
	 * 生成.json格式文件 参数说明： jsonString：json的字符串
	 * ；filePath：要把生成的json文件放到什么位置；fileName：给json文件的命名
	 * 返回值为布尔值：flag，默认为true，代表是否生成json文件成功
	 */
	public static boolean createJsonFile(String jsonString, String filePath, String fileName) throws Exception{
		boolean flag = true;

		// 拼接文件完整路径
		String fullPath = filePath + File.separator + fileName + ".json";

		// 生成json格式文件
		try {
			// 保证创建一个新文件
			File file = new File(fullPath);
			if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
				file.getParentFile().mkdirs();
			}
			if (file.exists()) { // 如果已存在,删除旧文件
				file.delete();
			}
			file.createNewFile();

			// 格式化json字符串
			jsonString = JsonFormatTool.formatJson(jsonString);
			// 将格式化后的字符串写入文件
			Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			write.write(jsonString);
			write.flush();
			write.close();
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}

		// 返回是否成功的标记
		return flag;
	}

}
