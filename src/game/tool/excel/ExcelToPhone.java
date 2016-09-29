package game.tool.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Sheet;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class ExcelToPhone extends ExcelTo {

	public ExcelToPhone(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg,
			String entityPackageName,String language) {
		super(pathConfFile, pathExportFile, pathFreemarker, showMsg, "c", entityPackageName,language);
	}

	@Override
	public void cleanGeneratedFile() {
		this.showMsgFront("===清理已生成的<手机端>数据\n");
		try {
			this.cleanFileBySuffix(".cpp", super.pathExportFile);
			this.cleanFileBySuffix(".h", super.pathExportFile);
			this.cleanFileBySuffix(".json", super.pathExportFile + "/json/");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void startGenProcess(String fileName, String sheetName) {
		try {

			fileName = "Conf" + fileName;
			this.genJSONFile(super.pathExportFile + "/json/", fileName, resultsClient);
			this.genPhoneConfEntity(fileName, sheetName);
		} catch (Exception e) {
			super.cleanContainer();
			e.printStackTrace();
		}

	}

	/**
	 * 生成手机端数据
	 * 
	 * @param entityName
	 * @param entityNameCN
	 * @throws Exception
	 */
	private void genPhoneConfEntity(String entityName, String entityNameCN) throws Exception {
		if (entityNameCN == null || entityNameCN.equals("")) {
			throw new Exception("执行中断!非法Sheet名称  <" + entityName + ">");
		}
		Configuration cfg = new Configuration();
		// 模板目录
		cfg.setDirectoryForTemplateLoading(new File(pathFreemarkerTmpl));
		// 设置对象包装器
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setEncoding(Locale.getDefault(), "UTF-8");

		// 使用的模板
		Template tempCPP = cfg.getTemplate("ExcelToCPP.ftl");
		Template tempH = cfg.getTemplate("ExcelToH.ftl");

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("className", entityName); // 类名
		root.put("idType", this.convertTypeToCPlus(idType)); // Id 数据类型

		Set<Map<String, String>> properties = new HashSet<>();
		root.put("properties", properties); // 字段名
		root.put("excelFileName", this.excelFileName); // Excel文件名

		if (clientInfos.isEmpty())
			return;

		// 遍历字段
		for (Entry<String, Map<String, String>> entity : clientInfos.entrySet()) {
			for (Entry<String, String> en : entity.getValue().entrySet()) {
				// 数据类型转换为 C++ 类型
				if (en.getKey().equals("type")) {
					en.setValue(this.convertTypeToCPlus(en.getValue()));
				}
			}
			// Java实体信息
			properties.add(entity.getValue());
		}
		this.writeFile(this.pathExportFile, entityName, root, tempCPP, ".cpp", "GBK");
		this.writeFile(this.pathExportFile, entityName, root, tempH, ".h", "GBK");
	}

	/**
	 * 将数据类型转为 C++
	 * 
	 * @param type
	 * @return
	 */
	private String convertTypeToCPlus(String type) {
		if (type.equals("boolean")) {
			return "bool";
		} else if (type.equals("long") || type.equals("Integer")) {
			return "int";
		}

		return type;
	}

	@Override
	protected void finalWork() {
		// 把所有单个文件组合到一个文件中 周雄需求
		try {
			super.showMsg("\n\n开始组合已生成的文件", true, false);
			this.processToOneFile();
			super.showMsg("\n文件组合结束，可以拷贝数据文件", true, false);
		} catch (Exception e) {
			e.printStackTrace();
			super.showMsg("合成文件时发生错误!", true, false);
		}
	}

	/**
	 * 组合单个文件
	 * 
	 * @throws Exception
	 */
	private void processToOneFile() throws Exception {
		File file = new File(this.pathExportFile);
		File[] files = file.listFiles();
		List<String> cppList = new ArrayList<String>();
		List<String> hList = new ArrayList<String>();
		for (File f : files) {
			if (f.getName().contains(".cpp"))
				cppList.add(this.readFile(f));
			if (f.getName().contains(".h"))
				hList.add(this.readFile(f));
		}
		// 写之前 先把单个文件删除
		this.cleanFileBySuffix(".cpp", this.pathExportFile);
		this.cleanFileBySuffix(".h", this.pathExportFile);

		this.genFinalFile(cppList, ".cpp");
		this.genFinalFile(hList, ".h");
	}

	/**
	 * 读取文件
	 * 
	 * @param file
	 * @return
	 */
	private String readFile(File file) {
		String fileContent = "";
		BufferedReader reader = null;
		try {
			String tempString = "";
			reader = new BufferedReader(new FileReader(file));
			while ((tempString = reader.readLine()) != null) {
				fileContent += tempString + "\n";
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
		return fileContent;
	}

	/**
	 * 生成大文件
	 * 
	 * @param list
	 * @param type
	 * @throws Exception
	 */
	private void genFinalFile(List<String> list, String type) throws Exception {
		Configuration cfg = new Configuration();
		// 模板目录
		cfg.setDirectoryForTemplateLoading(new File(pathFreemarkerTmpl));
		// 设置对象包装器
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setEncoding(Locale.getDefault(), "UTF-8");

		// 使用的模板
		Template temp = null;
		if (type.equals(".cpp"))
			temp = cfg.getTemplate("ExcelToCPPFile.ftl");
		else
			temp = cfg.getTemplate("ExcelToHFile.ftl");

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("fileContent", list);

		this.writeFile(this.pathExportFile, "JsonData", root, temp, type, "GBK");
	}

}
