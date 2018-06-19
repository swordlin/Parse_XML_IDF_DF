package com.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.data.Data;

public class FileTool {

	/*
	 * 存储BUG信息 filepath=E:/Results/Bug_msg/ filename=bug_msg_all
	 */
	public void store_bugs_data(Map<String, Vector<String>> data, String filepath, String filename) throws IOException {

		File file = new File(filepath);
		if (!file.exists()) {
			file.mkdirs();
		}

		File file1 = new File(filepath + filename);
		if (file1.exists()) {	//因为后续是append
			file1.delete();
		}
		//需要注意的是，这里的文件是append模式
		FileOutputStream outStream = new FileOutputStream(file1, true);
		
//		Comparator<String> comparator = new Comparator<String>(){
//
//			@Override
//			public int compare(String o1, String o2) {
//				// TODO Auto-generated method stub
//				return o2.compareTo(o1);
//			}
//			
//		};
//		List<Map.Entry<String,Vector<String>>> mapList = new ArrayList<>();
//		mapList.addAll(data.entrySet());
//		Comparator<Map.Entry<String, Vector<String>>> comparator = new Comparator<Map.Entry<String, Vector<String>>>(){
//
//			@Override
//			public int compare(Entry<String, Vector<String>> o1, Entry<String, Vector<String>> o2) {
//				// TODO Auto-generated method stub
//				return o1.getKey().compareTo(o2.getKey());
//			}
//			
//		};
//		Collections.sort(mapList, comparator);
		for (String key : data.keySet()) {
//		for (Map.Entry<String, Vector<String>> entry : mapList) {
//			System.out.println(key + "\t" + data.get(key).size());
			outStream.write((key + "\t").getBytes());// bug_id
			outStream.write((data.get(key).get(4) + "\t").getBytes());// assigned_to
			outStream.write((data.get(key).get(1) + "\t").getBytes());// resolution
			outStream.write((data.get(key).get(3) + "\t").getBytes());// dup_id
			outStream.write((data.get(key).get(5) + "\t").getBytes());	//creation_ts
			outStream.write((data.get(key).get(6) + "\t").getBytes());	//delta_ts
			outStream.write((data.get(key).get(7) + "\t").getBytes());	//product
			outStream.write((data.get(key).get(8)).getBytes());	//component
//			outStream.write((entry.getKey() + "\t").getBytes());// bug_id
//			outStream.write(((entry.getValue().get(4)) + "\t").getBytes());
//			outStream.write(((entry.getValue().get(1)) + "\t").getBytes());
//			outStream.write(((entry.getValue().get(3)) + "\t").getBytes());
//			outStream.write(((entry.getValue().get(5)) + "\t").getBytes());
//			outStream.write(((entry.getValue().get(6)) + "\t").getBytes());
//			outStream.write(((entry.getValue().get(7)) + "\t").getBytes());
//			outStream.write(((entry.getValue().get(8))).getBytes());

			outStream.write(("\n").getBytes());

		}

		outStream.flush();
		outStream.close();
	}

	/*
	 * 读取词干干处理之后的的数据,用于生成sensorNameList
	 */
	public static void Read_words_stemed(String stemmed_pathString) throws IOException {
		// stemmed_pathString=E:/Results/Bug_msg/keywords_stemed/
		File file_stem = new File(stemmed_pathString);
		String word;

		File[] sampleDir = file_stem.listFiles();// keywords_stemed/下所有文件

		for (int i = 0; i < sampleDir.length; i++) {
			// stemed
//			String filenameString = sampleDir[i].getName();
//			if (filenameString.contains("stemed")) {
//				String regExp = "\\D";// 只保留数字
//				String[] nums = filenameString.split(regExp);
//				String id = nums[0];

				 String id = sampleDir[i].getName();//不词干化
				
				FileReader samReader = new FileReader(sampleDir[i]);
				BufferedReader samBR = new BufferedReader(samReader);

				Map<String, Integer> bug_words = new HashMap<String, Integer>();
				Vector<String> diff_words = new Vector<String>();//每个bug中stemed下的不同词
				Vector<String> id_words_stemed = new Vector<>();//每个bug中stemed下的所有词
				while ((word = samBR.readLine()) != null) {
					if (!word.isEmpty() && (!Data.words_stemed_all.contains(word))) {// 不为空，且，当前数组不包含该数据
						Data.words_stemed_all.add(word);
					}
					if (!word.isEmpty() && !diff_words.contains(word)) {
						diff_words.add(word);
					}
					// 有则累加，没有则设为1；add 存储累加频度的词汇
					if (!word.isEmpty()) {
						if (bug_words.containsKey(word)) {
							int pindu = bug_words.get(word) + 1;
							bug_words.put(word, pindu);
						} else {
							bug_words.put(word, 1);
						}
					}
					//每个bug中stemed下的所有词
					if (!word.isEmpty()) {// 不为空
						id_words_stemed.add(word);
					}

				} // while
				Data.bugId_diffWord.put(id, diff_words);
				Data.each_desc_msg_stemed_all.put(id, id_words_stemed);
				samBR.close();
				samReader.close();
//			} //if
		}// for

	}

	/*
	 * 读取词干处理之后的的数据，用于生成feature_matrix
	 */
	public static Map<String, Vector<String>> Read_each_desc_words_stemed(String stemmed_pathString)
			throws IOException {
		File file_stem = new File(stemmed_pathString);
		String word;
		Map<String, Vector<String>> xml_msg_stemed = new HashMap<String, Vector<String>>();

		File[] sampleDir = file_stem.listFiles();

		for (int i = 0; i < sampleDir.length; i++) {
			String id = sampleDir[i].getName();

			// if(filenameString.contains("stemed")){
			// String regExp="\\D";//只保留数字
			// String [] nums=filenameString.split(regExp);//
			// String id = nums[0];
			Vector<String> id_words_stemed = new Vector<>();

			FileReader samReader = new FileReader(sampleDir[i]);
			BufferedReader samBR = new BufferedReader(samReader);

			while ((word = samBR.readLine()) != null) {
				if (!word.isEmpty()) {// 不为空
					id_words_stemed.add(word);
				}
			}

			xml_msg_stemed.put(id, id_words_stemed);
			// }
		}

		return xml_msg_stemed;
	}

	/*
	 * 存储所有词干化之后的词
	 */
	public static void store_words_stemed(Vector<String> words_stemed, String filename) throws IOException {
		// filename=E:/Results/Bug_msg/sensorNameList
		// words_stemed=Data.words_stemed_all
		File file = new File(filename);
		FileOutputStream outStream = new FileOutputStream(file, true);
		for (int i = 0; i < words_stemed.size(); i++) {
			outStream.write(words_stemed.get(i).getBytes());
			outStream.write("\n".getBytes());
		}
		outStream.flush();
		outStream.close();
	}

	//存储未加权特征矩阵
	public static void store_Feature_Matrix(int[][] Feature_Matrix, String filename) throws IOException {
		File file = new File(filename);
		FileOutputStream outStream = new FileOutputStream(file, true);
		for (int i = 0; i < Feature_Matrix.length; i++) {
			for (int j = 0; j < Feature_Matrix[i].length; j++) {
				outStream.write((String.valueOf(Feature_Matrix[i][j]) + " ").getBytes());
			}
			outStream.write("\n".getBytes());
		}
		outStream.flush();
		outStream.close();
	}

	//存储加权的特征矩阵
	public static void store_Feature_Matrix(Double[][] Feature_Matrix, String filename) throws IOException {
		File file = new File(filename);
		FileOutputStream outStream = new FileOutputStream(file, true);
		for (int i = 0; i < Feature_Matrix.length; i++) {
			for (int j = 0; j < Feature_Matrix[i].length; j++) {
				outStream.write((String.valueOf(Feature_Matrix[i][j]) + " ").getBytes());
			}
			outStream.write("\n".getBytes());
		}
		outStream.flush();
		outStream.close();
	}

	/*
	 * 存储剩余的5000个词
	 */
	public static void end_word(String root, String filepath) {
		try {
			String path = root + filepath;
			File file = new File(path);
			FileOutputStream outStream = new FileOutputStream(file, true);

			for (int i = 0; i < Data.words_stemed_all.size(); i++) {
				outStream.write(Data.words_stemed_all.get(i).getBytes());
				outStream.write("\n".getBytes());
			}

			outStream.flush();
			outStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 存储矩阵每行对应的bugId及开发者
	 */
	public static void Store_bugId_dep(String root, String filepath) {
		try {
			String path = root + filepath;
			File file = new File(path);
			FileOutputStream outStream;

			outStream = new FileOutputStream(file, true);

			for (int i = 0; i < Data.store_bugId_Order.size(); i++) {
				String bugId = Data.store_bugId_Order.get(i);
				// 输出每行矩阵对应的bugId
				outStream.write((bugId + "===").getBytes());
				// 输出每行矩阵对应的开发者
				outStream.write((Data.xml_msg_all.get(bugId).get(4) + "===").getBytes());
				outStream.write(("\n").getBytes());
			}

			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
