package game.tool.excel;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JTextArea;

import org.apache.poi.ss.usermodel.Sheet;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class ExcelToClient extends ExcelTo {

	public ExcelToClient(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg,
			String entityPackageName,String language) {
		super(pathConfFile, pathExportFile, pathFreemarker, showMsg, "c", entityPackageName,language);
	}

	private String pathASFile;

	// 清理已生成的文件
	@Override
	public void cleanGeneratedFile() {
		super.showMsgFront("===清理已生成的<客户端>数据\n");
		pathASFile = super.pathExportFile + "file/";
		try {
			// 客户端文件
			this.cleanFileBySuffix(".json", super.pathExportFile);
			this.cleanFileBySuffix(".as", pathASFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startGenProcess(String fileName, String sheetName) {

		try {
			// 生成AS文件
			this.genASFile(fileName, sheetName);
			fileName = "Conf" + fileName;
			// 生成JSON 文件
			this.genJSONFile(super.pathExportFile, fileName, resultsClient);

		} catch (Exception e) {
			// 清理数据容器
			this.cleanContainer();
			e.printStackTrace();
		}
	}

	/**
	 * 生成AS文件
	 * 
	 * @param entityName
	 * @param entityNameCN
	 * @param filePath
	 * @throws Exception
	 */
	private void genASFile(String entityName, String entityNameCN) throws Exception {

		Configuration cfg = new Configuration();
		// 模板目录
		cfg.setDirectoryForTemplateLoading(new File(pathFreemarkerTmpl));
		// 设置对象包装器
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		cfg.setEncoding(Locale.getDefault(), "UTF-8");

		// 使用的模板
		Template temp = cfg.getTemplate("ExcelToAS.ftl");
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("entityName", entityName); // 类名
		root.put("excelFileName", super.excelFileName); // Excel文件名
		root.put("entityNameCN", entityNameCN); // 类注释

		Set<Map<String, String>> properties = new HashSet<>();
		root.put("properties", properties); // 字段名

		if (clientInfos.isEmpty())
			return;

		// 遍历字段
		for (Entry<String, Map<String, String>> entity : clientInfos.entrySet()) {
			// Java实体信息
			properties.add(entity.getValue());
		}

		super.writeFile(pathASFile, entityName + "VO", root, temp, ".as", "UTF-8");
	}

	@Override
	protected void finalWork() {
		// TODO Auto-generated method stub
	}

}
