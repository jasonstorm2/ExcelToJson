package game.tool.excel;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JTextArea;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class ExcelToServer extends ExcelTo {

	public ExcelToServer(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg,
			String entityPackageName ,String language) {
		super(pathConfFile, pathExportFile, pathFreemarker, showMsg, "s", entityPackageName,language);

	}

	@Override
	public void cleanGeneratedFile() {
		super.showMsgFront("===清理已生成的<服务端>数据\n");
		try {
			super.cleanFileBySuffix(".json", this.getJsonPath());

			super.cleanFileBySuffix(".java", this.getEntityPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startGenProcess(String fileName, String sheetName) {
		if (entityPackageName == null || entityPackageName.equals("")) {
			super.showMsgFront("===java包名没有设置 \n");
			return;
		}
		try {
			fileName = "Conf" + fileName;
			// 生成JSON数据文件
			this.genJSONFile(this.getJsonPath(), fileName, resultsServer);
			// 生成实体类
			genJavaConfEntity(fileName, sheetName);
		} catch (Exception e) {
			// 清理数据容器
			this.cleanContainer();
			e.printStackTrace();
		}

	}

	/**
	 * 生成 java 实体
	 * 
	 * @param entityName
	 * @param filePath
	 * @param packageName
	 * @param entityInfos
	 * @throws Exception
	 */
	private void genJavaConfEntity(String entityName, String entityNameCN) throws Exception {
		if (entityNameCN == null || entityNameCN.equals("")) {
			super.showMsgFront("执行中断!非法Sheet名称  <" + entityName + ">");
			return;
		}
		// 验证定义的字段是否有大小写不同的重复值
		super.checkFieldNameRepeat(entityInfos);

		Configuration cfg = new Configuration();
		// 模板目录
		cfg.setDirectoryForTemplateLoading(new File(pathFreemarkerTmpl));
		// 设置对象包装器
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setEncoding(Locale.getDefault(), "UTF-8");

		// 使用的模板
		Template temp = cfg.getTemplate("ExcelToJava.ftl");

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("entityName", entityName); // 类名
		root.put("excelFileName", this.excelFileName); // Excel文件名
		root.put("packageName", entityPackageName); // 包名
		root.put("entityNameCN", entityNameCN); // 类注释
		root.put("idType", super.idType); // Id 数据类型

		Set<Map<String, String>> properties = new LinkedHashSet<>();
		root.put("properties", properties); // 字段名

		String paramMethod = ""; // 构造器方法中的参数
		String paramInit = ""; // 初始化方法中的参数

		if (entityInfos.isEmpty())
			return;

		int records = 0;
		// 遍历字段
		for (Entry<String, Map<String, String>> entity : entityInfos.entrySet()) {
			// Java实体信息
			properties.add(entity.getValue());
			// 拼构造方法的参数字符串
			paramMethod += entity.getValue().get("type") + " " + entity.getValue().get("name");

			// 拼初始化方法中需要的字符串
			String type = entity.getValue().get("type");
			if (type.equalsIgnoreCase("int")) {
				paramInit += "conf.getIntValue(\"" + entity.getValue().get("name") + "\")";
			} else if (type.equalsIgnoreCase("boolean")) {
				paramInit += "conf.getBooleanValue(\"" + entity.getValue().get("name") + "\")";
			} else if (type.equalsIgnoreCase("double")) {
				paramInit += "conf.getDoubleValue(\"" + entity.getValue().get("name") + "\")";
			} else if (type.equalsIgnoreCase("long")) {
				paramInit += "conf.getLongValue(\"" + entity.getValue().get("name") + "\")";
			} else if (type.equalsIgnoreCase("float")) {
				paramInit += "conf.getFloatValue(\"" + entity.getValue().get("name") + "\")";
			} else if (type.equalsIgnoreCase("float[]")) {
				paramInit += "parseFloatArray(conf.getString(\"" + entity.getValue().get("name") + "\"))";
			} else if (type.equalsIgnoreCase("double[]")) {
				paramInit += "parseDoubleArray(conf.getString(\"" + entity.getValue().get("name") + "\"))";
			} else if (type.equalsIgnoreCase("String[]")) {
				paramInit += "parseStringArray(conf.getString(\"" + entity.getValue().get("name") + "\"))";
			} else if (type.equalsIgnoreCase("int[]")) {
				paramInit += "parseIntArray(conf.getString(\"" + entity.getValue().get("name") + "\"))";
			} else if (type.equalsIgnoreCase("boolean[]")) {
				paramInit += "parseBoolArray(conf.getString(\"" + entity.getValue().get("name") + "\"))";
			} else if (type.equalsIgnoreCase("long[]")) {
				paramInit += "parseLongArray(conf.getString(\"" + entity.getValue().get("name") + "\"))";
			} else {
				paramInit += "conf.getString(\"" + entity.getValue().get("name") + "\")";
			}

			records++;
			if (records != entityInfos.size()) {
				paramMethod += ", ";
				paramInit += ", ";
			}
			// 每遍历4个 换一行
			if (records % 4 == 0 && records != entityInfos.size()) {
				paramInit += "\n\t\t\t\t";
			}

		}

		root.put("paramMethod", paramMethod);
		root.put("paramInit", paramInit);

		this.writeFile(this.getEntityPath(), entityName, root, temp, ".java", "UTF-8");
	}

	private String getEntityPath() {
		String newPath = "";
		if (super.pathExportFile.contains("web"))
			newPath = super.pathExportFile.replace("web/", "");
		else
			newPath = super.pathExportFile.replace("phone/", "");
		return newPath;
	}

	private String getJsonPath() {
		String newPath = super.pathExportFile + "/json/";
		// if(super.pathExportFile.contains("web")) {
		// newPath = super.pathExportFile.replace("web/", "");
		// newPath += "json/web/";
		// }else{
		// newPath = super.pathExportFile.replace("phone/", "");
		// newPath += "json/phone/";
		// }
		return newPath;
	}

	@Override
	protected void finalWork() {

	}

}
