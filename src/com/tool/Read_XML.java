package com.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.data.Data;

public class Read_XML {

	String id = "";
	String short_desc = "";
	String resolution = "";
	String long_desc = "";
	String dup_id = "";
	String assigned_to = "";
	String cc = "";
	String creation_ts = "";
	String delta_ts = "";
	String product = "";
	String component = "";
	int count;

	/*
	 * 解析该XML文件夹下面的所有XML文件
	 */
	public void parse_XML_Eclipse_all(String filepath) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(filepath);
		File[] filelist = file.listFiles();
		System.out.println("filelist.size() = " + filelist.length);

		for (int i = 0; i < filelist.length; i++) {
			String path = filepath + filelist[i].getName();
			String sname = path.substring(path.lastIndexOf("."));
			if (!sname.equals(".bak")) {// 不解xibakhou
				System.out.println(path);
				read_xml(path);
			}

		}
		System.out.println("只筛选状态为Fixed与duplicated后所剩bug数目：： " + Data.xml_msg_all.size());
		deldup_ID();// 将有重复标签的bug,其重复bugID不在所选bug中删除，在的话改为重复bug的标签。
		delLessTen();

	}

	/*
	 * 统计map中dup_Id在key中存在的bug,将该bug删除(改标签)
	 */
	public void deldup_ID() {
		String developer;
		Vector<String> tempbug = new Vector<>();
		for (String key : Data.xml_msg_all.keySet()) {
			if (Data.xml_msg_all.get(key).get(3) != "kong"
					&& Data.xml_msg_all.get(key).get(1).equalsIgnoreCase("DUPLICATE")) {
				// 获取重复的id号
				String dupid = Data.xml_msg_all.get(key).get(3);//
				if (Data.xml_msg_all.keySet().contains(dupid)) {
					// 获取被重复的bug类别
					developer = Data.xml_msg_all.get(dupid).get(4);//
					Vector<String> tempdep = new Vector<>();
					tempdep.add(Data.xml_msg_all.get(key).get(0));	//把key的数据中的assignee换成dupid的assignee
					tempdep.add(Data.xml_msg_all.get(key).get(1));	//其他保持不变
					tempdep.add(Data.xml_msg_all.get(key).get(2));
					tempdep.add(Data.xml_msg_all.get(key).get(3));
					tempdep.add(developer);
					tempdep.add(Data.xml_msg_all.get(key).get(5));
					tempdep.add(Data.xml_msg_all.get(key).get(6));		//注意语句之间彼此的顺序
					tempdep.add(Data.xml_msg_all.get(key).get(7));
					tempdep.add(Data.xml_msg_all.get(key).get(8));
					// 标签替换
					Data.xml_msg_all.put(key, tempdep);		//后期新增节点的时候， 标签替换这里忘记新增add了3
				} else {	//标记的重复ID 不在当前数据集中，就把重复ID的载体key放进tempbug中
					tempbug.add(key);
				}
			}
		}

		//把tempbug中存的key删掉
		// 假设B里的重复id是A，可A由于各种原因（比如状态不是fixed）没有放入Data.xml_msg_all中， 那我们就把B这条数据
		//从Data.xml_msg_all中删掉， 因为我们宁愿牺牲它来避免噪声
		for (int i = 0; i < tempbug.size(); i++) {
			if (Data.xml_msg_all.keySet().contains(tempbug.get(i)))
				;
			Data.xml_msg_all.remove(tempbug.get(i));
		}
		System.out.println("删除重复不在key中的bug啦！！**********");
	}

	/*
	 * 统计map中每一个处理bug少于10的开发者， 将其对应的bug报告全部删除 得到预处理后的bug,存储在新的map中
	 */
	public void delLessTen() {
		// 存储不同的开发者及其处理bug数量
		Map<String, Integer> reporter = new HashMap<String, Integer>();
		int temp;
		for (Map.Entry<String, Vector<String>> entry1 : Data.xml_msg_all.entrySet()) {
			// 如果report的key中不包含遍历到的开发者，将此开发者加到report的key中
			// 否则对应value值加1
			if (!(reporter.keySet().contains(entry1.getValue().get(4)))) {
				reporter.put((String) entry1.getValue().get(4), 1);
			} else {
				temp = reporter.get(entry1.getValue().get(4));
				temp++;
				reporter.put((String) entry1.getValue().get(4), temp);
			}
		}
		System.out.println("reporter：不同开发者的数量~~~~~~~~~~~~~~~~~~~~~~ " + reporter.size());

		// 删除处理bug数量小于10的开发者
		for (Map.Entry<String, Integer> entry2 : reporter.entrySet()) {
			Vector<String> tempbug2 = new Vector<>();
			if (entry2.getValue() < 10) {
				for (Map.Entry<String, Vector<String>> entry3 : Data.xml_msg_all.entrySet()) {
					if (entry3.getValue().get(4).equals(entry2.getKey())) {
						// 获取处理bug数量小于10的开发者处理的所有bug key值,
						tempbug2.add(entry3.getKey());
					} else {
						continue;
					}
				}
			} else {
				continue;
			}

			for (int i = 0; i < tempbug2.size(); i++) {
				if (Data.xml_msg_all.keySet().contains(tempbug2.get(i)))
					;
				Data.xml_msg_all.remove(tempbug2.get(i));
			}
		}
		System.out.println("最终留下bug条数************* " + Data.xml_msg_all.size());

		// 将处理大于等于10的开发者输出保存
		keep_developer(reporter);
	}

	// 将处理大于等于10的开发者输出保存
	public void keep_developer(Map<String, Integer> reporter) {
		int i = 0;
		String filepath = Data.Store_path + "/";
		File file = new File(filepath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File file1 = new File(filepath + "developer");
		if (file1.exists()) {
			file1.delete();
		}
		try {
			FileOutputStream outStream = new FileOutputStream(file1, true);
			for (Map.Entry<String, Integer> entry : reporter.entrySet()) {
				if (entry.getValue() >= 10) {
					i++;
					outStream.write(entry.getKey().getBytes());
					outStream.write(("\n").getBytes());
				}
			}
			System.out.println("··················大于等于10的人数··········" + i + "·························");
			outStream.flush();
			outStream.close();
		} catch (IOException e) {
		}

	}

	public void read_xml(String path) throws ParserConfigurationException, SAXException, IOException {

		File f = new File(path);// 每100个bug的地址
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// 不解析XML中的dtd文件
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(f);
		NodeList nodelist = doc.getElementsByTagName("bug");

		if (nodelist != null) {
			for (int i = 0; i < nodelist.getLength(); i++) {// 遍历BUG

				Node child = nodelist.item(i);//
				NodeList childlist = child.getChildNodes();
				Vector<String> bug_msg = new Vector<String>();
				init();// 初始化数组,将值全部置0
				count = 1;// 表示第几个长描述
				for (int j = 0; j < childlist.getLength(); j++) {

					if (childlist.item(j).getNodeType() == Node.ELEMENT_NODE) {

						if (childlist.item(j).getNodeName() == "bug_id") {
							id = childlist.item(j).getFirstChild().getNodeValue();// id
							Data.child_msg[0] = id.trim();
							// System.out.println(id + "**********");
						}
						if (childlist.item(j).getNodeName() == "short_desc") {
							short_desc = childlist.item(j).getFirstChild().getNodeValue();// bug_severity
							Data.child_msg[1] = short_desc;
							// System.out.println(short_desc + "**********");
						}
						if (childlist.item(j).getNodeName() == "resolution") {
							resolution = childlist.item(j).getFirstChild().getNodeValue();// bug_severity
							Data.child_msg[2] = resolution.trim();
							// System.out.println(resolution +
							// "**************");
						}
						
						//获取第一个长描述
						if (childlist.item(j).getNodeName() == "long_desc" && count == 1) {
							Node sunzi = childlist.item(j);//
							NodeList sunzilist = sunzi.getChildNodes();
							for (int k = 0; k < sunzilist.getLength(); k++) {
								if (sunzilist.item(k).getNodeName() == "thetext") {
									if (sunzilist.item(k).getTextContent() != null
											&& (!sunzilist.item(k).getTextContent().isEmpty())) {
										long_desc = sunzilist.item(k).getFirstChild().getNodeValue();
									}else{
										Data.child_msg[3]=" ";
									}
								}
							}
							Data.child_msg[3] = long_desc.trim();
							count++;
						}

						if (childlist.item(j).getNodeName() == "dup_id") {
							dup_id = childlist.item(j).getFirstChild().getNodeValue();// bug_severity
							Data.child_msg[4] = dup_id.trim();
							// System.out.println(dup_id + "**********");
						}
						if (Data.child_msg[4].isEmpty()) {
							Data.child_msg[4] = "kong";
						}
						if (childlist.item(j).getNodeName() == "assigned_to") {
							assigned_to = childlist.item(j).getFirstChild().getNodeValue();// id
							Data.child_msg[5] = assigned_to.trim();
							// System.out.println(id + "**********");
						}
						if (childlist.item(j).getNodeName() == "cc") {// 最后一个cc
							cc = childlist.item(j).getFirstChild().getNodeValue();// bug_severity
							Data.child_msg[5] = cc;
						}
						//以下是新增的对两个时间的提取
						if (childlist.item(j).getNodeName() == "creation_ts") {
							creation_ts = childlist.item(j).getFirstChild().getNodeValue();
							Data.child_msg[6] = creation_ts;
						}
						if (childlist.item(j).getNodeName() == "delta_ts") {
							delta_ts = childlist.item(j).getFirstChild().getNodeValue();
							Data.child_msg[7] = delta_ts;
						}
						//对所属产品信息的提取
						if (childlist.item(j).getNodeName() == "product") {
							product = childlist.item(j).getFirstChild().getNodeValue();
							Data.child_msg[8] = product;
						}
						//所属component信息
						if (childlist.item(j).getNodeName() == "component") {
							component = childlist.item(j).getFirstChild().getNodeValue();
							Data.child_msg[9] = component;
						}

					}
				}

				bug_msg.clear();
				for (int k = 0; k < Data.child_msg.length; k++) {
					if (!Data.child_msg[k].equalsIgnoreCase("")) {
						bug_msg.add(Data.child_msg[k]);
					}
				}

				if (bug_msg.size() == 10) {		//别忘记改这里， 说实话。。。这块的复用好是麻烦， 搁以前二话不说就重写了，可惜现在老了没激情了

					add_bugs_msg(bug_msg); // 分阶段存储数据

				}
			}
		}

	}

	/*
	 * 数据分布式存储
	 */
	public void add_bugs_msg(Vector<String> bug_msg) {
		String id_1 = bug_msg.get(0);
		String shortdesc_1 = bug_msg.get(1);
		String resolution_1 = bug_msg.get(2);
		String longdes_1 = bug_msg.get(3);
		String dupId_1 = bug_msg.get(4);
		String assignedTo_1 = bug_msg.get(5);
		String creation_ts = bug_msg.get(6);
		String delta_ts = bug_msg.get(7);
		String product = bug_msg.get(8);
		String component = bug_msg.get(9);

		// 只提取状态为FIXED和DUPLICATE的bug

		if (resolution_1.equalsIgnoreCase("FIXED") || resolution_1.equalsIgnoreCase("DUPLICATE")) {
			Vector<String> bug = new Vector<>();

			bug.add(shortdesc_1);
			bug.add(resolution_1);
			bug.add(longdes_1);
			bug.add(dupId_1);
			bug.add(assignedTo_1);
			bug.add(creation_ts);
			bug.add(delta_ts);
			bug.add(product);
			bug.add(component);
			// System.out.println("id:" + id_1);
			Data.xml_msg_all.put(id_1, bug);

		}
	}

	/*
	 * 初始化
	 */
	public void init() {
		for (int i = 0; i < Data.child_msg.length; i++) {
			Data.child_msg[i] = "";
		}
	}// init

	public static void read_stop_words(String stopwords_path) {
		try {
			FileReader stopWordsReader = new FileReader(stopwords_path);
			BufferedReader stopWordsBR = new BufferedReader(stopWordsReader);
			String line, resLine, stopWordsLine;
			while ((stopWordsLine = stopWordsBR.readLine()) != null) {
				if (!stopWordsLine.isEmpty() && !stopWordsLine.equalsIgnoreCase("")) {
					Data.stopWords_all.add(stopWordsLine);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// read_stop_words

}
