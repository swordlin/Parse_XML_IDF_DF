package com.kk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.data.Data;
import com.tool.DataPreProcess;
import com.tool.FileTool;
import com.tool.Read_XML;

public class KTCmodel {

	Read_XML rx = new Read_XML();
	FileTool ft = new FileTool();
	DataPreProcess dpp = new DataPreProcess();

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		KTCmodel xmodel = new KTCmodel();

		xmodel.Parse_Eclipse(Data.Eclipse_bug_path);

		System.out.println("DONE!");
	}

	private void Parse_Eclipse(String eclipse_bug_path) throws ParserConfigurationException, SAXException, IOException {
		// 存储停用词
        rx.read_stop_words(Data.stopwords_path);
		// 解析XML数据
		rx.parse_XML_Eclipse_all(eclipse_bug_path);
		System.out.println("最终所剩bug条数：：" + Data.xml_msg_all.size());
//		System.out.println(Data.xml_msg_all.get("37").size());
		// 存储BUG信息
		String bugs_filename_root = Data.Store_path ;
		ft.store_bugs_data(Data.xml_msg_all, bugs_filename_root, "bug_msg_all");
		
		// 词干化，去停词处理；dpp.BPPMain完成后将每个bug中的词都存储起来
		String bugs_filename_eclipse_stemed = bugs_filename_root + "keywords_stemed/";
		dpp.BPPMain(Data.xml_msg_all, bugs_filename_eclipse_stemed);// 将处理好的数据存储

		// 处理成标准数据型存储；
		FileTool.Read_words_stemed(bugs_filename_eclipse_stemed);// 读取目前所有的关键字，存储到Data.words_stemed_all
		String words_filepath_name = bugs_filename_root + "sensorNameList";// 存储所有不同词的路径
		// 存储未精简的所有词
		FileTool.store_words_stemed(Data.words_stemed_all, words_filepath_name);// 存储单词列表
//		Data.each_desc_msg_stemed_all = FileTool.Read_each_desc_words_stemed(bugs_filename_eclipse_stemed);// 存储到Data.xml_msg_stemed_all
		// 将特征词的数量缩减到5000
		
//		reduce_5000();
		
		// 生成特征矩阵
//		Gen_Feature_Matrix(bugs_filename_root);
	}

	private void Gen_Feature_Matrix(String bugs_filename_root) throws IOException {
		int bugs_num = Data.each_desc_msg_stemed_all.size();// 矩阵行数
		
		// 下面二选一（全部的词与约简到8000个词）
		 int words_num = Data.words_stemed_all.size();//列；改
//		int words_num = Data.words_5000.size();// 矩阵列数

		System.out.println("行" + bugs_num + ",列" + words_num);
		int[][] Feature_Matrix = new int[bugs_num][words_num];

		int id = 0;

		// 遍历所有bug,生成特征矩阵。
		for (String key : Data.each_desc_msg_stemed_all.keySet()) {
			
			Data.store_bugId_Order.add(key);// 将存储矩阵的顺序记录下来bug_id
			// 获取对应bug的所有词
			Vector<String> id_bug_words = Data.each_desc_msg_stemed_all.get(key);
			Map<String, Integer> bug_words = new HashMap<String, Integer>();// 单词
																			// +频度
			for (int i = 0; i < id_bug_words.size(); i++) {

				String word_stemed = id_bug_words.get(i);
				// add 存储累加频度的词汇
				// 有则累加，没有则设为1
				if (bug_words.containsKey(word_stemed)) {
					int pindu = bug_words.get(word_stemed) + 1;
					bug_words.put(word_stemed, pindu);
				} else {
					bug_words.put(word_stemed, 1);
				}
			}
			// 单词是有顺序的列表
			for (int j = 0; j < words_num; j++) { // words_num=5000
				// String stemmed_word = Data.words_5000.get(j);//
				// Data.words_stemed_all改为存储的新的5000个词
				String stemmed_word = Data.words_stemed_all.get(j);
				if (bug_words.containsKey(stemmed_word)) {//
					Feature_Matrix[id][j] = bug_words.get(stemmed_word);
				} else {
					Feature_Matrix[id][j] = 0;
				}
			}

			id++;// id行号
		}

		ft.Store_bugId_dep(bugs_filename_root, "Matrix_perHang_bugId_dep");// 先将矩阵每行bugId及开发者存储，释放Data.xml_msg_all
		Store_String_Word();// 存储StringToVector所需文件，存储每行bug中的词,可重复

		Data.xml_msg_all.clear();// 释放内存空间
		Data.words_stemed_all.clear();// 释放内存空间
		Data.words_5000.clear();
		Data.bugId_diffWord.clear();
		Data.each_desc_msg_stemed_all.clear();// 释放内存空间

		// 存储不加权的特征值矩阵
		String Feature_Matrix_filepath = bugs_filename_root + "feature_matrix";
		FileTool.store_Feature_Matrix(Feature_Matrix, Feature_Matrix_filepath);
		System.out.println("未加权矩阵存储完毕");

		// 存储加权后的特征矩阵
//		Double[][] KV_IDF_TF = cal_Frequency(Feature_Matrix, words_num, bugs_num);// Frequency
//		System.out.println("加权矩阵计算完成");
//		String Feature_Matrix_IDF_TF_filepath = bugs_filename_root + "KV_IDF_TF";
//		FileTool.store_Feature_Matrix(KV_IDF_TF, Feature_Matrix_IDF_TF_filepath);
	}

	/*
	 * 计算IDF*TF
	 */
	private Double[][] cal_Frequency(int[][] feature_Matrix, int words_nums, int bugs_nums) {
		// TODO Auto-generated method stub
		int words_num = words_nums;// 列；改(二选一)
		// int words_num = Data.words_5000.size();// 列
		int bugs_num = bugs_nums;// 行数

		Double[][] Feature_Matrix_IDF_TF = new Double[bugs_num][words_num];

		// 计算频度
		Double[][] TF = new Double[bugs_num][words_num];
		Double[] IDF = new Double[words_num];

		// 计算TF
		Integer[] words_totals_shuzu = new Integer[bugs_num];//每行特征值的加和
		for (int i = 0; i < bugs_num; i++) {//行
			int words_totals = 0;
			for (int j = 0; j < words_num; j++) {
				words_totals = words_totals + feature_Matrix[i][j];
			}
			words_totals_shuzu[i] = words_totals;
		}//for

		for (int i = 0; i < bugs_num; i++) {
			for (int j = 0; j < words_num; j++) {
				// TF是每一个格一个值
				if (words_totals_shuzu[i] != 0) {
					TF[i][j] = (double) feature_Matrix[i][j] / words_totals_shuzu[i];
				} else {
					TF[i][j] = 0.0;
				}
			}
		}//for

		// 计算IDF
		for (int i = 0; i < words_num; i++) {
			int doc_num = 0;
			for (int j = 0; j < bugs_num; j++) {
				if (feature_Matrix[j][i] > 0) {
					doc_num = doc_num + 1;
				}
			}
			// IDF是每一列一个值
			if (doc_num > 0) {
				IDF[i] = Math.log((double) bugs_num / doc_num);
			} else {
				IDF[i] = 0.0;
			}
		}//for

		// TF*IDF
		for (int i = 0; i < bugs_num; i++) {
			for (int j = 0; j < words_num; j++) {
				Feature_Matrix_IDF_TF[i][j] = TF[i][j] * IDF[j];
			}
		}

		return Feature_Matrix_IDF_TF;
	}

	public static void reduce_5000() {
		int lie = Data.words_stemed_all.size();
		String[] words = new String[lie];
		int[] times = new int[lie];
		int index = 0;
		String word = null;
		for (String data : Data.words_stemed_all) {
			words[index++] = data;
		}
		for (int k = 0; k < lie; k++) {
			word = words[k];
			int time = 0;
			for (String key : Data.bugId_diffWord.keySet()) {
				if (Data.bugId_diffWord.get(key).contains(word)) {
					time++;
				}
			}
			times[k] = time;
		}

		// 冒泡排序
		for (int i = 0; i < lie; i++) { // 鏈�澶氬仛n-1瓒熸帓搴�
			for (int j = 0; j < lie - i - 1; j++) { // 瀵瑰綋鍓嶆棤搴忓尯闂磗core[0......length-i-1]杩涜鎺掑簭(j鐨勮寖鍥村緢鍏抽敭锛岃繖涓寖鍥存槸鍦ㄩ�愭缂╁皬鐨�)
				if (times[j] < times[j + 1]) { // 鎶婂皬鐨勫�间氦鎹㈠埌鍚庨潰
					int temp1 = times[j];
					times[j] = times[j + 1];
					times[j + 1] = temp1;

					String temp2 = words[j];
					words[j] = words[j + 1];
					words[j + 1] = temp2;
				}
			}
		}

		System.out.println("保留的前5000个词中排前10的词与词频");
		for (int i = 0; i < 10; i++) {
			System.out.print(words[i] + ":" + times[i] + "次； ");
		}
		System.out.println();

		// 取前8000个词
		for (int m = 0; m < 8818; m++) {
			Data.words_5000.add(words[m]);
		}

	}

	public static void Store_String_Word() {
		try {
			File file = new File(Data.Store_path + "StringToVector_matix");
			FileOutputStream outStream = new FileOutputStream(file, true);
			for(int i=0;i<Data.store_bugId_Order.size();i++){
				Vector<String> PerHang_Word = new Vector<>();// 临时记录每行bug包含的所有词
				PerHang_Word = Data.each_desc_msg_stemed_all.get(Data.store_bugId_Order.get(i));
				for (int j = 0; j < PerHang_Word.size(); j++) {
					outStream.write((PerHang_Word.get(j) + " ").getBytes());
				}
				outStream.write("\n".getBytes());
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
