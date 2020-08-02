package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class DemoApplication {
	static JdbcTemplate jdbcTemplate ;

	public static void test(String path, String target) throws IOException {
		FileInputStream inputStream = null;
		Scanner sc = null;
		FileOutputStream fileOutputStream = null;
		File outFile = null;
		try {
			inputStream = new FileInputStream(path);

			outFile = new File(target);
			fileOutputStream = new FileOutputStream(outFile);


			sc = new Scanner(inputStream, "UTF-8");

			while (sc.hasNextLine()) {
				//读取一行文件数据
				String line = sc.nextLine();

				//获取读取数据库
				List<Map<String, Object>> list = readDB();

				//处理数据
				int start = line.indexOf("('");
				int end = line.indexOf("',");
				String newLine = line.substring(0, start+2) + (String)list.get(0).get("name") + line.substring(end) + "\n";


				//写到新文件中（内存）
				fileOutputStream.write(newLine.getBytes());
				//flush到磁盘
				fileOutputStream.flush();

			}
			// note that Scanner suppresses exceptions
			if (sc.ioException() != null) {
				throw sc.ioException();
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			if (sc != null) {
				sc.close();
			}
		}
	}

	public static List<Map<String, Object>> readDB(){
		List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM user");
//		System.out.println(result);
		return result;
	}

	public static void insertDBFromFile(String path) throws Exception {
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {
			inputStream = new FileInputStream(path);

			sc = new Scanner(inputStream, "UTF-8");

			while (sc.hasNextLine()) {
				//读取一行文件数据
				String line = sc.nextLine();

				//插入数据库
				if(!insetDBOneLine(line)){
					System.out.println("insert DB faild: " + line);
				}

			}
			// note that Scanner suppresses exceptions
			if (sc.ioException() != null) {
				throw sc.ioException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (sc != null) {
				sc.close();
			}
		}
	}

	public  static boolean insetDBOneLine(String command){
		return jdbcTemplate.update(command) > 0;
	}

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
		jdbcTemplate = context.getBean(JdbcTemplate.class);

		String path = "/Users/ruibo/Documents/a.sql";
		String targ = "/Users/ruibo/Documents/a.sql.temp";
		test(path, targ);

		//TODO:. 读取文件一行并执行
		insertDBFromFile(targ);
	}



}
