package com.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Data {

	public static String Eclipse_bug_path = "/home/wanglinhui/bug_triaging/eclipse-bugs/";
	
	public static String Store_path = "/home/wanglinhui/bug_triaging/Results/Bug_msg_eclipse/";
	public static String Results_path = "/home/wanglinhui/bug_triaging/Results/";
	public static String stopwords_path = "/home/wanglinhui/bug_triaging/Foxstoplist.txt";
	
	public static ArrayList<String> stopWords_all = new ArrayList<String>();//stopword，存储750个停用词
	public static int notkong = 0;
	
	//<bugId,<短描述,状态,长描述,dup_Id,类别(assignee)， creation_ts, delta_ts, product,component>>
	public static Map<String, Vector<String>> xml_msg_all = new HashMap<String, Vector<String>>();//所有bug信息：desc+product+component+severity
	public static String [] child_msg = new String[10];//临时存储；
	
	//所有bug中的不同词(19778)
	public static Vector<String> words_stemed_all = new Vector<>();
	//存储所有bug的bug_id,及该bug中可能重复的所有词。
	public static Map<String, Vector<String>> each_desc_msg_stemed_all = new HashMap<String, Vector<String>>();//所有stemed的bug信息
	//存储所有bug的bug_id及该bug中不同的词及该词的词频
//	public static Map<String, Map<String, Integer>> Each_bug_stemmed_words = new HashMap<String, Map<String, Integer>>();//Id + <diff_word+频度>
	//存储每个bug的id，及该bug中所含有的不同的词。
	public static Map<String, Vector<String>> bugId_diffWord = new HashMap<String, Vector<String>>();//所有bug中不同的词
	//存储词频高的5000个词
	public static Vector<String> words_5000 = new Vector<>();
	//矩阵对应的bugId存储的先后顺序
	public static ArrayList<String> store_bugId_Order = new ArrayList<String>();
}
