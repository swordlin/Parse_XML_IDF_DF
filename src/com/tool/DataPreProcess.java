package com.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.data.Data;

/**
 * Newsgroups文档集预处理类
 */
public class DataPreProcess {
	static int bug_sum = 0;

	/*
	 * 预处理 strDir=E:/Results/Bug_msg/keywords_stemed/ 
	 * data=xml_msg_all 词干化处理
	 */
	public void doProcess(Map<String, Vector<String>> data, String strDir) throws IOException {

		File fileTarget = new File(strDir);
		if (!fileTarget.exists()) {// 注意processedSample需要先建立目录建出来，否则会报错，因为母目录不存在
			fileTarget.mkdir();
		}

		// 存储的bugid
		String[] stemFileNames = new String[data.size()];
		int i = 0;
		for (String key : data.keySet()) {// 遍历xml_msg_all
			String fileShortName = key;// id=filename

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(strDir + fileShortName);// 字符串连接

			// 分词、去停词
			// data.get(key).get(0) == desc
			//短描述加第一个长描述
			createProcessFile_gsk(data.get(key).get(0) , data.get(key).get(2), stringBuilder.toString());
			//只有短描述
//			createProcessFile_gsk(data.get(key).get(0), stringBuilder.toString());
			
			stemFileNames[i] = stringBuilder.toString();
			i++;
		}

		// 下面调用stem算法，词干化，去停词。并保存
		//Stemmer.porterMain(stemFileNames);//
		// stemFileNames为所有未词干化存储bugId词的文件路径（数组）（）
	}

	/**
	 * 进行文本预处理生成目标文件
	 * 
	 * @param srcDir
	 *            源文件文件目录的绝对路径
	 * @param targetDir
	 *            生成的目标文件的绝对路径
	 * @throws IOException
	 */
	public static void createProcessFile_gsk(String str1,String str2, String targetDir) throws IOException {
		// Str为获取的每一个bug的短描述加长描述
		// targetDir：生成文件的路径E:/......../1;2;3..........
		
		FileWriter targetFileWriter = new FileWriter(targetDir);
		String line, resLine;
//		String str=str1+" "+str2;
		// 返回处理好的一行字符串，是由处理好的单词重新生成，以空格为分隔符
//		String short_desc=lineProcess(str1, Data.stopWords_all);//仅短描述
		resLine = lineProcess(str1, str2, Data.stopWords_all);// desc，stopwords
		
//		if(!short_desc.isEmpty()){
//			String[] tempStr = resLine.split(" ");
//			for (int i = 0; i < tempStr.length; i++) {
//				if (!tempStr[i].isEmpty() && (!isNumeric1(tempStr[i]))) {
//					for(int k=0;k<4;k++){
//						targetFileWriter.append(tempStr[i] + "\n");
//					}
//				}
//			}
//		}
		
		if (!resLine.isEmpty()) {
			// 按行写，一行写一个单词
			String[] tempStr = resLine.split(" ");//
			for (int i = 0; i < tempStr.length; i++) {
				if (!tempStr[i].isEmpty() && (!isNumeric1(tempStr[i]))) {
					targetFileWriter.append(tempStr[i] + "\n");
				}
			}
		}
		targetFileWriter.flush();
		targetFileWriter.close();
	}

	// 判断一个字符串是否为数字
	public static boolean isNumeric1(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 对每行字符串进行处理，主要是词法分析、去停用词和stemming
	 * 
	 * @param line
	 *            待处理的一行字符串
	 * @param ArrayList<String>
	 *            停用词数组
	 * @return String 处理好的一行字符串，是由处理好的单词重新生成，以空格为分隔符
	 * @throws IOException
	 */
	private static String lineProcess(String line1, String line2, ArrayList<String> stopWordsArray) throws IOException {
		
		System.out.println(bug_sum++);
		String return_Str = null;
		String end_String;
		Vector<String> vec = new Vector<String>();
		
		//短描述乘10加长描述
//		String str1=" ";
//		for(int i=0;i<10;i++){
//			str1=str1+" "+line1;
//		}
//		String line=str1+" "+line2;
		String line=line1+" "+line2;	//短描述+长描述
		String line_low = line.toLowerCase();// 把字符串转换成小写
		end_String = line_low.replace("/", " ");
		end_String = end_String.replace(";", " ");
		end_String = end_String.replace("?", " ");
		end_String = end_String.replace(".", " ");
		end_String = end_String.replace(":", " ");
		end_String = end_String.replace("//", " ");
		end_String = end_String.replace("&lt", " ");
		end_String = end_String.replace("&gt", " ");
		end_String = end_String.replace("&quot", " ");
		end_String = end_String.replace("_", " ");
		end_String = end_String.replace("-", " ");
		end_String = end_String.replace(",", " ");
		end_String = end_String.replace("(", " ");
		end_String = end_String.replace(")", " ");
		end_String = end_String.replace("=", " ");
		end_String = end_String.replace("$", " ");
		end_String = end_String.replace("!", " ");
		end_String = end_String.replace("@", " ");
		end_String = end_String.replace("*", " ");
		end_String = end_String.replace("%", " ");
		end_String = end_String.replace("&", " ");
		end_String = end_String.replace("{", " ");
		end_String = end_String.replace("}", " ");
		end_String = end_String.replace("+", " ");
		end_String = end_String.replace("[", " ");
		end_String = end_String.replace("]", " ");
		end_String = end_String.replace("'", " ");
		end_String = end_String.replace("\"", " ");
		end_String = end_String.replace("~", " ");
		end_String = end_String.replace("<", " ");
		end_String = end_String.replace(">", " ");
		end_String = end_String.replace("#", " ");
		end_String = end_String.replace("”", " ");
		end_String = end_String.replace("“", " ");// \
		end_String = end_String.replace("\\", " ");
		end_String = end_String.replace("|", " ");
		end_String = end_String.replace("\t", " ");
//		end_String = end_String.replace("0", " ");
//		end_String = end_String.replace("1", " ");
//		end_String = end_String.replace("2", " ");
//		end_String = end_String.replace("3", " ");
//		end_String = end_String.replace("4", " ");
//		end_String = end_String.replace("5", " ");
//		end_String = end_String.replace("6", " ");
//		end_String = end_String.replace("7", " ");
//		end_String = end_String.replace("8", " ");
//		end_String = end_String.replace("9", " ");
		String arr[] = end_String.split(" ");
		for (int i = 0; i < arr.length; i++) {
			if ((java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(arr[i]))) {//判断字符串是否为乱码
				if (arr[i].length() >= 1 && !isInteger(arr[i]) && (!arr[i].contains("\n"))) {
					vec.add(arr[i]);
				}
			}
		}
		// 去停用词
		Vector<String> vec2 = new Vector<String>();
		for (int i = 0; i < vec.size(); i++) {
			if (!stopWordsArray.contains(vec.get(i))) {
				vec2.add(vec.get(i));
			}
		}
		// 返回处理后的词，以空格分开
		for (int i = 0; i < vec2.size(); i++) {
			if (i == 0) {
				return_Str = vec2.get(i);
			} else {
				return_Str = return_Str + " " + vec2.get(i);
			}
		}
		return return_Str;

	}

	/*
	 * 判断是否为整数
	 * 
	 * @param str 传入的字符串
	 * 
	 * @return 是整数返回true,否则返回false
	 */
	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	/**
	 * @param args
	 * @throws IOException
	 *             path=E:/Results/Bug_msg/Moizlla_Core_Networking/
	 *             keywords_stemed/ data=xml_msg_all
	 */
	public void BPPMain(Map<String, Vector<String>> data, String path) throws IOException {
		// TODO Auto-generated method stub
		DataPreProcess dataPrePro = new DataPreProcess();
		dataPrePro.doProcess(data, path);

	}

}
