package game.tool.excel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import freemarker.template.Template;

public abstract class ExcelTo extends Thread {

	private int flagCS = 0; // 区分 客户端/服务器 定义的所在行
	private int flagType = 1; // Java数据类型 定义的所在行
	private int flagNameEN = 2; // 字段名称 定义的所在行
	private int flagNameCN = 3; // 字段中文名 定义的所在行
	
	private final String  SUB = "sub";  //母表中子表的标志
	private final String SUBSN = "subsn";//母表中子表sn标志
	
	private int executeType = 2;//1：大领主编译表格模式 2：EVA编译表格模式
	
	
	

	public String pathConfFile; // 配置文件所在路径
	public String pathExportFile; // 文件导出路径
	public String pathFreemarkerTmpl; // 生成代码模板路径
	// java 包名
	public String entityPackageName;
	//指定的编译语言
	public String language;

	public JTextArea showMsg; // 用来显示操作信息的控件
	// 错误文件记录
	public static List<String> errorFiles = new ArrayList<>();
	public String excelFileName; // Excel文件名 显示在实体头上
	public String idType = "String"; // Id(SN)类型

	//表和文件名
	Map<String,String> nameMap = new HashMap<String,String>();

	// 数据
	List<Map<Object, Object>> resultsServer = new ArrayList<Map<Object, Object>>();
	List<Map<Object, Object>> resultsClient = new ArrayList<Map<Object, Object>>();

	// 服务器端实体信息
	Map<String, Map<String, String>> entityInfos = new LinkedHashMap<>(); // 记录java实体信息
	Map<String, Map<String, String>> clientInfos = new LinkedHashMap<>(); // 记录java实体信息
	List<Object> ids = new ArrayList<>(); // 记录所有Id，判断是否有重复

	public String CSSign; // 服务器/客户端标识

	public boolean escap = false; // 按esc中断执行
	// 验证定义的字段是否有重复的
	public List<String> fieldNames = new ArrayList<String>();

	// 处理文件总数
	int countConfFile = 0;

	@Override
	public void run() {
		this.init();
		this.readExcel();
		this.finalWork();
	}

	// 生成文件后的收尾工作
	protected abstract void finalWork();

	public void init() {
		countConfFile = 0;
		this.ids.clear();
		if (this.CSSign == null || "".equals(CSSign)) {
			this.showMsgBack("===没有设置文件生成类型(服务器/客户端)");
		}
		if (this.pathConfFile == null || "".equals(this.pathConfFile)) {
			this.showMsgFront("===游戏配置文件的路径没有设置");
			return;
		}
		if (this.pathExportFile == null || "".equals(this.pathExportFile)) {
			this.showMsgFront("===文件生成目录没有设置");
			return;
		}
		if (this.pathFreemarkerTmpl == null || "".equals(this.pathFreemarkerTmpl)) {
			this.showMsgFront("===生成文件模板路径没有设置");
			return;
		}
		// 错误文件记录
		errorFiles = new ArrayList<>();
		// 清除已生成的文件
		this.cleanGeneratedFile();
		this.showMsg.requestFocus();
	}

	// 设置路径
	public ExcelTo(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg, String CSSign,
			String entityPackageName,String language) {
		this.pathConfFile = pathConfFile;
		this.pathExportFile = pathExportFile;
		this.pathFreemarkerTmpl = pathFreemarker;
		this.showMsg = showMsg;
		this.CSSign = CSSign;
		this.entityPackageName = entityPackageName;
		this.language = language;
	};

	/**
	 * 读取 Excel 文件
	 */
	public void readExcel() {
		// 读取Excel文件
		String path = this.pathConfFile;		
		File f = new File(path);
		if (!f.isDirectory()) {
			this.showMsgFront("===游戏配置路径设置错误:" + this.pathConfFile);
			return;
		}
		// 获得文件
		File[] files = f.listFiles();		
		// 处理Excel文件
		this.processExcel(files);
		

	}

	/**
	 * 处理Excel
	 * 
	 * @param files
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InvalidFormatException 
	 */
	public void processExcel(File[] files) {		
		int numSucc = 0;// 成功处理的文件个数
		int numError = 0;// 忽略处理的文件个数
		// 表和文件名收集
		switch (executeType) {
		case 1:
			nameMap = filePathMap(files);
			break;
		case 2:
			nameMap = filePathMap2(files);
			break;
		default:
			break;
		}
		
		// 遍历文件
		for (int i = 0; i < files.length; i++) {
			if (escap)
				break;
			String filePathName = files[i].getPath();	
			// 去掉../
			filePathName = this.processPath(filePathName);
			// 只处理 xls 和 xlsx的文件
			if (!filePathName.endsWith(".xls") && !filePathName.endsWith(".xlsx") && !filePathName.endsWith("xlsm")) {
				this.showMsgFront("\n===非法配置文件，忽略处理! " + filePathName);
				continue;
			}
			if (filePathName.indexOf("~$") != -1) {
				this.showMsgFront("\n===临时文件，忽略处理! " + filePathName);
				continue;
			}
			// 记录文件名 显示在配置文件头部
			this.excelFileName = files[i].getName();
			this.showMsg("\n======开始处理: <" + filePathName + ">======", true, true);
			// 记录处理文件数量
			countConfFile++;
			// 处理Work Book 生成逻辑在这个方法里
			boolean ret = false;
			switch (executeType) {
			case 1:
				ret = this.processWorkBook(filePathName);
				break;
			case 2:
				ret = this.processWorkBook2(filePathName);
				break;
			default:
				break;
			}		
			
			if (ret)
				numSucc++;
			else
				numError++;
		}
		this.showMsgFront("\n===处理完毕，EXCEL文件总个数：" + countConfFile);
		this.showMsgFront("\n===处理成功个数：" + numSucc);
		if (numError > 0)
			this.showMsgFront("\n===处理失败个数：" + numError);
	}
	


	/**
	 * 获得 Excel work book
	 * 
	 * @param excelFile
	 * @param ip
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public boolean processWorkBook(String excelFile) {
		boolean ret = false;
		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(new File(excelFile)));
			// 将表按英文名分类
			Map<String, List<String>> map = this.processSameSheet(workbook);
			if (map.isEmpty()) {
				this.showMsgBack("表中没有任何有效数据, 忽略处理!");
				ret = false;
			} else {
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					String fileName = entry.getKey();
					String sheetName = "";

					for (String str : entry.getValue()) {
						Sheet sheet = workbook.getSheet(str);
						sheetName += sheet.getSheetName() + " ";
						// 处理sheet的数据
						this.processSheet(sheet, excelFile);
					}
					// 开始生成处理
					this.startGenProcess(fileName, sheetName);
					// 生成完的清除工作
					this.cleanContainer();
				}
				ret = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.showMsgFront("===处理<" + excelFile + ">时异常，原因: " + e.getMessage() + "    ->右键打开错误文件\n");
			this.addErrorFile(excelFile);
		}
		return ret;
	}
	
	/**
	 * 
	 * @param excelFile
	 * @return
	 */
	public boolean processWorkBook2(String excelFile) {
		boolean ret = false;
		String language = this.language;
		File file = new File(excelFile);
		String name = getFileName(file.getName());		
		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(new File(excelFile)));		
				// 处理sheet的数据
				Sheet sheet = workbook.getSheet(language);
				if(sheet == null){
					return ret;
				}
				this.processSheet(sheet, excelFile);
				// 开始生成处理
				this.startGenProcess(name, language);
				// 生成完的清除工作
				this.cleanContainer();			
				ret = true;
		
		} catch (Exception e) {
			e.printStackTrace();
			this.showMsgFront("===处理<" + excelFile + ">时异常，原因: " + e.getMessage() + "    ->右键打开错误文件\n");
			this.addErrorFile(excelFile);
		}
		return ret;
	}
	
	/**
	 * 裁剪表格名字 activity_活动.xlsx
	 * @param name
	 * @return
	 */
	public String getFileName(String name){
		
		if(name.contains(".")){
			name = name.substring(0,name.lastIndexOf("."));
		}
		if(name.contains("_")){
			name = name.substring(0,name.lastIndexOf("_"));
		}
		return name;
	}

	

	/**
	 * 
	 * @param sheet
	 * @param excelFile 只用于抛出的异常
	 * @throws Exception
	 */
	 
	@SuppressWarnings("unchecked")
	private void processSheet(Sheet sheet, String excelFile) throws Exception {
		// 每张表清空重复字段记录（英文名字）
		fieldNames.clear();
		// 导入数据的总行数
		int totalRow = sheet.getLastRowNum();
		// 没有数据
		if (totalRow == 0) {
			throw new Exception("===sheet：" + sheet.getSheetName() + ", 没有数据,请检查\n");
		}
		// 导入数据总列数
		int totalColume = sheet.getRow(0).getLastCellNum();
		
		/* 表头数据采集 */
		// 表格的列
		for (int colume = 0; colume < totalColume; colume++) {
			if (escap)
				break;
			try {
				// 忽略没有定义 CS 头的列
				Cell cellSOrC = sheet.getRow(flagCS).getCell(colume);
				if (cellSOrC == null || cellSOrC.getCellType() == Cell.CELL_TYPE_BLANK) {
					continue;
				}
				// 服务器或者客户端标识
				String serverOrClient = cellSOrC.getStringCellValue().toLowerCase();
				if(!serverOrClient.contains("c") && !serverOrClient.contains("s")){
					//表头定义出错的
					continue;
				}
				// 定义好的头信息
				this.saveHeadInfo(sheet, colume, serverOrClient);
			} catch (Exception e) {
				this.showMsgFront("===sheet：" + sheet.getSheetName() + ", " + 1 + " 行, "
						+ this.convertNumToLetter(colume) + " 列, 数据异常,可能是：" + e.getMessage() + "   ->右键打开错误文件\n");
				this.addErrorFile(excelFile);
				e.printStackTrace();
			}
		}
		
		/* 具体数据采集 */
		resultsServer = (List<Map<Object, Object>>) getSheetData(sheet, null);
		System.out.println("");
	}

	/**
	 * 根据该CELL的数据类型，返回该数据类型的对象，包装成OBJECT，如 String,boolean 等
	 * @param sheet
	 * @param cell
	 * @param dateType
	 * @return
	 * @throws Exception
	 */
	private Object processCellAndGetDate(Sheet sheet, Cell cell, String dateType) throws Exception {

		// 设置默认值
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			cell = this.setDefaultValue(sheet, dateType);
		}

		// 处理数据
		switch (cell.getCellType()) {
		// 数字类型
		case Cell.CELL_TYPE_NUMERIC: {
			// 去除科学计数法
			return this.formatSciNot(cell.getNumericCellValue());
		}
		// 字符串类型
		case Cell.CELL_TYPE_STRING: {
			String value = cell.getStringCellValue();
			// JSON格式 校验格式
			if (dateType != null && dateType.equalsIgnoreCase("json")) {
				this.checkJSONFormat(value);
			}
			return value;
		}
		// boolean类型
		case Cell.CELL_TYPE_BOOLEAN: {
			if (dateType != null && !dateType.equals("boolean") && !dateType.equals("String"))
				throw new Exception("===数据类型不应当是Boolean类型");
			// 客户端
			return cell.getBooleanCellValue();
		}
		// 公式类型
		case Cell.CELL_TYPE_FORMULA: {
			return this.checkDataForFormula(cell, dateType);
		}
		default: {
			throw new Exception("===未知数据类型！");
		}

		}
	}

	/**
	 * 设置默认值
	 * 
	 * @param sheet
	 * @param colume
	 */
	public Cell setDefaultValue(Sheet sheet, String dateType) {

		// 如果单元为空 在最大单元格创建默认值
		Cell cell = sheet.createRow(255).createCell(255);
		if (dateType.equalsIgnoreCase("int") || dateType.equalsIgnoreCase("long") || dateType.equalsIgnoreCase("float")) {
			cell.setCellValue(0);
		} else if (dateType.equalsIgnoreCase("double")) {
			cell.setCellValue(0.0);
		} else if (dateType.equalsIgnoreCase("boolean")) {
			cell.setCellValue(false);
		} else if (dateType.equalsIgnoreCase("json")) {
			cell.setCellValue("{}");
		} else {
			cell.setCellValue("");
		}

		return cell;
	}

	/**
	 * 检查SN
	 * 
	 * @param cell
	 * @throws Exception
	 */
	public void checkSnAndSaveType(Cell cell, String dateType) throws Exception {
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			throw new Exception("ID列不能为空!");
		}

		String sn = null;
		// POI会按当前单元格类型读取数据
		sn = getCellValue(cell);

		if (ids.contains(sn))
			throw new Exception("ID中有重复的值: " + sn);
		if (sn == null ||"".equals(sn))
			throw new Exception("ID不能为空字符串！");
		ids.add(sn);

		// 记录 ID(SN) 列数据类型 FreeMark 中使用
		idType = dateType.equalsIgnoreCase("int") ? "Integer" : dateType;
	}
	
	public String getCellValue(Cell cell) {
		String value = null;
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_FORMULA:
				// cell.getCellFormula();
				try {
					value = String.valueOf(cell.getNumericCellValue());
				} catch (IllegalStateException e) {
					value = String.valueOf(cell.getRichStringCellValue());
				}
				break;
			case Cell.CELL_TYPE_NUMERIC:
				value = String.valueOf(cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_STRING:
				value = String.valueOf(cell.getRichStringCellValue());
				break;
			}
		}
		return value;
	}

	/**
	 * 记录服务器端定义的字段信息
	 * 
	 * @param sheet
	 * @param infoMap
	 * @param row
	 * @param colume
	 */
	public void saveHeadInfo(Sheet sheet, int colume, String clientOrServer)
			throws Exception {
		// java实体的 字段类型、名称、注释 {type=int, name=sn(英文名), note=(中文名)天数}
		Map<String, String> infoMap = new LinkedHashMap<>();
		// 数据类型
		String dateType = sheet.getRow(flagType).getCell(colume).getStringCellValue();
		//如果是子表的表示
		if(dateType.equals(SUBSN)){
			return;
		}

		String nameEn = sheet.getRow(flagNameEN).getCell(colume).getStringCellValue();

		// 记录数据类型
		infoMap.put("type", dateType.equalsIgnoreCase("json")||dateType.equalsIgnoreCase(SUB) ? "String" : dateType);// json,sub 记录为 String类型
		// 字段名称
		// 统一 SN 大小写
		if (colume == 0) {
			nameEn = this.toSaveCase(nameEn.toUpperCase());
		}
		infoMap.put("name", nameEn);
		// 代码中的注释
		String note = sheet.getRow(flagNameCN).getCell(colume).getStringCellValue();
		// 替换回车
		note = note.replaceAll("\n", "");
		infoMap.put("note", note);
		if (clientOrServer.contains("s")) {
			entityInfos.put(nameEn, infoMap);
		}

		if (clientOrServer.contains("c")) {
			clientInfos.put(nameEn, infoMap);
		}

		// 验证重复定义字段
		if (fieldNames.contains(nameEn.toLowerCase())) {
			throw new Exception("有重复定义字段 " + nameEn);
		} 

		fieldNames.add(nameEn.toLowerCase());
		System.out.println();
		//子表的字段信息合并
	}

	/**
	 * 清除已生成的文件
	 */
	public abstract void cleanGeneratedFile();

	// 前台显示
	public void showMsgFront(String content) {
		this.showMsg(content, true, false);
	}

	// 后台显示
	public void showMsgBack(String content) {
		this.showMsg(content, false, true);
	}

	/**
	 * 显示日志
	 * 
	 * @param content
	 */
	public void showMsg(String content, boolean isShowFront, boolean isShowBack) {
		if (showMsg != null && isShowFront) {
			showMsg.append(content);
			// 日志窗口滚动
			this.scrollScreen();
		}
		// 是否在后台显示
		if (isShowBack) {
			System.out.println(content);
		}
	}

	/**
	 * 删除目录下指定后缀的文件
	 * 
	 * @param suffix
	 * @param filePath
	 * @throws Exception
	 */
	public void cleanFileBySuffix(String suffix, String filePath) throws Exception {
		File f = new File(filePath);
		String[] files = f.list();

		if (f == null || !f.exists()) {
			throw new Exception("===清理时未发现目录，稍后将会自动创建: " + filePath + "\n");

		}
		for (String fileName : files) {
			File file = new File(filePath + fileName);

			if (fileName.endsWith(suffix))
				file.delete();
		}
	}

	/**
	 * 去掉../
	 * 
	 * @param path
	 * @return
	 */
	private String processPath(String path) {
		if (path == null)
			return path;
		int pos = path.indexOf("..\\");
		if (pos == -1)
			return path;

		String path1 = path.substring(0, pos - 1);
		path1 = path1.substring(0, path1.lastIndexOf("\\"));
		String path2 = path.substring(pos + 2, path.length());
		return path1 + path2;
	}

	/**
	 * 屏幕滚动
	 */
	public void scrollScreen() {
		if (showMsg != null) {
			showMsg.setCaretPosition(showMsg.getText().length());
		}
	}

	/**
	 * 处理有前缀的sheet,如 "任务|Quest_1" 命名的sheet
	 * 
	 * @param sheet
	 * @return
	 */
	public Map<String, String> checkPrefix(Sheet sheet) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		String name = sheet.getSheetName();
		if (name.startsWith("Sheet"))
			return map;// add by shenjh,忽略Sheet开头的表

		if (name.contains("_")) {
			name = name.substring(0, name.indexOf("_"));
		}

		if (!name.contains("|")) {
			this.showMsgBack("===sheet：" + sheet.getSheetName() + " , 非规则命名，忽略处理!");
			return map;
		}

		// 类注释 中文
		map.put("entityNameCN", name.substring(0, name.indexOf("|")));
		// 类名
		map.put("entityName", name.substring(name.indexOf("|") + 1, name.length()));

		return map;
	}

	/**
	 * sheet名称中 是否只包含中文
	 * 
	 * @param str
	 * @return
	 */
	private boolean findChineseFromStr(String str) {
		Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	/**
	 * 统一大小写
	 * 
	 * @param str
	 * @return
	 */
	private String toSaveCase(String str) {
		return str.toLowerCase();
	}

	/**
	 * 数据转为指定对象 添加新数据类型时 别忘添加 genJavaConfEntity 方法里的处理
	 * 
	 * @param dateType
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private void convertDataByTypeAndSetIntoMap(Map<Object, Object> map, String fieldNameEN, String dateType,
			Object value) throws Exception {
		if (dateType.equalsIgnoreCase("int")) {
			String valueStr = String.valueOf(value);
			if (valueStr.indexOf("0x") != -1) {
				map.put(fieldNameEN, Integer.decode(String.valueOf(value)));
			} else {// 有时会把配置的 11010 读成 11010.0,这样直接转int就会报错，所以先转double 然后强转为int, 因为定义的就是int 所以小数点后没有了也不影响
				// 强转前先判断是否超出最大值
				if (Double.parseDouble(valueStr) > Integer.MAX_VALUE) {
					throw new Exception("int类型超出最大值!");
				}
				map.put(fieldNameEN, (int) Double.parseDouble(valueStr));
			}
		} else if (dateType.equalsIgnoreCase("long")) {
			String valueStr = String.valueOf(value);
			map.put(fieldNameEN, Long.parseLong(valueStr));
		} else if (dateType.equalsIgnoreCase("double")) {
			String valueStr = String.valueOf(value);
			map.put(fieldNameEN, new Double(valueStr).doubleValue());
		} else if (dateType.equalsIgnoreCase("String") || dateType.equalsIgnoreCase("double[]")
				|| dateType.equalsIgnoreCase("int[]") || dateType.equalsIgnoreCase("float[]")
				|| dateType.equalsIgnoreCase("string[]") || dateType.equalsIgnoreCase("long[]")
				|| dateType.equalsIgnoreCase("boolean[]")|| dateType.equalsIgnoreCase("sub")) {
			map.put(fieldNameEN, value);
		} else if (dateType.equalsIgnoreCase("json")) {
			map.put(fieldNameEN, this.convert0xData(value.toString()));
		} else if (dateType.equalsIgnoreCase("boolean")) {
			String valueStr = String.valueOf(value);
			if (valueStr.equals("0"))
				valueStr = "false"; // 支持 0 1试的boolean类型配置
			if (valueStr.equals("1"))
				valueStr = "true";
			if (!valueStr.equalsIgnoreCase("true") && !valueStr.equalsIgnoreCase("false")) {
				throw new Exception("===布尔类型的数据定义错误");
			}
			map.put(fieldNameEN, Boolean.valueOf(valueStr));
		} else if (dateType.equalsIgnoreCase("float")) {
			map.put(fieldNameEN, Float.valueOf((String) value));
		} else {
			throw new Exception("===未知数据类型：" + dateType);
		}
	}

	/**
	 * 处理公式型数据
	 * 
	 * @param map
	 * @param type
	 * @param cell
	 */
	private Object checkDataForFormula(Cell cell, String dateType) {

		String value = "";
		// 公式数据 有可能是字符串的 也可能是数字的，所以先尝试用字符串的方法取
		try {
			// 用字符串的方法取，即使取不到抛异常也不用关心，下面还会用数字的方法取
			value = cell.getStringCellValue();
		} catch (IllegalStateException e) {

		}
		// 如果取到，包含0x的处理
		if (value.indexOf("0x") != -1) {
			return Integer.decode(value);
		} else if (!"".equals(value)) { // 如果取到不为空 返回数据
			return value;
		} else { // 最后再用数字类型尝试
			// 去除科学计数法
			return this.formatSciNot(cell.getNumericCellValue());
		}

	}

	/**
	 * 去除科学计数法
	 * 
	 * @param value
	 * @return
	 */
	private String formatSciNot(double value) {
		BigDecimal bd = new BigDecimal(value);

		return bd.toString();
	}

	/**
	 * 检查数据为JSON的格式
	 * 
	 * @param json
	 */
	private void checkJSONFormat(String json) throws Exception {
		if (!json.contains("{") && !json.contains("[")) {
			throw new Exception("===JSON数据验证失败!");
		}
		json = this.convert0xData(json);
		// 验证JSON格式
		this.checkJsonParse(json);
	}

	/**
	 * 将[[0x10001010, 0x10002010],[0x10002010]] 这种数据变为实际的数值,只支持10位的!! 防止json验证问题，以及数据读取时的问题
	 * 
	 * @param str
	 * @return
	 */
	private String convert0xData(String str) {

		if (!str.contains("0x"))
			return str;

		String tmp[] = str.split(",");

		for (String s : tmp) {
			// 获得 0x1000101
			String s1 = s.substring(s.indexOf("0x"), (s.indexOf("0x") + 10));
			// 将0x1000101 解析成数字后 再转成字符串准备替换
			String s2 = String.valueOf(Integer.decode(s1));
			// 将解析后的数字替换0x10000101
			str = str.replaceAll(s1, s2);
		}

		return str;
	}

	/**
	 * 将数字装换为字母顺序
	 * 
	 * @param num
	 * @return
	 */
	private String convertNumToLetter(int num) {
		String[] letter = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
				"S", "T", "U", "V", "W", "X", "Y", "Z" };
		if (num > 25) {
			int tenPos = num / 26 - 1;
			int onePos = num % 26;
			return letter[tenPos] + letter[onePos];
		}
		return letter[num];
	}

	/**
	 * 记录错误文件
	 * 
	 * @param filePath
	 */
	public void addErrorFile(String filePath) {
		if (!errorFiles.contains(filePath))
			errorFiles.add(filePath);
	}

	/**
	 * 生成处理
	 * 
	 * @param fileName
	 * @param sheetName
	 *            sheet的名称，用来生成文件后，可以对应找到相应的Sheet表格
	 */
	public abstract void startGenProcess(String fileName, String sheetName);

	// 清除容器
	public void cleanContainer() {
		// 清除已写入的容器
		resultsServer.clear();
		resultsClient.clear();
		entityInfos.clear();
		clientInfos.clear();
		ids.clear();
	}

	/**
	 * 写文件
	 * 
	 * @param filePath
	 * @param entityName
	 * @param root
	 * @param temp
	 * @throws Exception
	 */
	public void writeFile(String filePath, String entityName, Map<String, Object> root, Template temp, String suffix,
			String code) throws Exception {
		// 创建路径
		File path = new File(filePath);
		if (!path.exists()) {
			this.showMsgBack("===目录创建成功: " + filePath + "\n");
			path.mkdirs();
		}

		// 生成文件
		String filePathNameJava = filePath + entityName + suffix;
		File file = new File(filePathNameJava);
		if (!file.exists()) {
			file.createNewFile();
		} else {
			throw new Exception("===生成时发生错误!重名文件存在:" + file.getAbsolutePath() + "\n");
		}
		// 写文件
		FileOutputStream writerStream = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(writerStream, code));
		temp.process(root, writer);
		writer.flush();
		writer.close();
	}

	/**
	 * 生成JSON数据文件
	 * 
	 * @throws IOException
	 */
	public void genJSONFile(String filePath, String name, List<Map<Object, Object>> resultList) throws Exception {
		// if(resultList.isEmpty()) return; JSON数据为空时 生成一个空文件 防止java实体初始化时报错

		String filePathName = filePath + name + ".json";
		File file = new File(filePathName);

		// 创建路径
		File path = new File(filePath);
		if (!path.exists()) {
			this.showMsgBack("===目录创建成功: " + filePath + "\n");
			path.mkdirs();
		}

		if (!file.exists()) {
			this.showMsgBack("===创建文件：" + filePathName);
			file.createNewFile();
		} else {
			throw new Exception("===重名文件存在：" + filePathName + "\n");
		}

		FileOutputStream writerStream = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));
		// JSONTools.format() 格式化后JSON文件会变大 注掉
		String jsonStr = JSON.toJSONString(resultList);
		writer.write(jsonStr);
		writer.close();
	}

	/**
	 * 将表分类 为方便同样的表一起处理
	 * 
	 * @param workbook
	 * @return
	 */
	private Map<String, List<String>> processSameSheet(Workbook workbook) {
		int totalSheet = workbook.getNumberOfSheets();
		// 表分类 同样的表放在一起
		Map<String, List<String>> map = new HashMap<>();

		for (int i = 0; i < totalSheet; i++) {
			if (escap)
				break;

			Sheet sheet = workbook.getSheetAt(i);
			// 表名
			String name = sheet.getSheetName();
			if (name.startsWith("Sheet"))
				continue;// add by shenjh,忽略Sheet开头的表

			// 非法命名忽略
			if (!name.contains("|")) {
				this.showMsgBack("===sheet：" + sheet.getSheetName() + " , 非规则命名，忽略处理!");
				continue;
			}

			// 表英文名
			String nameEN = name.substring(name.indexOf("|") + 1, name.length());

			if (map.get(nameEN) == null) {
				map.put(nameEN, new ArrayList<String>());
			}
			//同名的表可以放在一起
			map.get(nameEN).add(name);
		}
		return map;
	}

	/**
	 * 验证JSON 字符串
	 * 
	 * @param json
	 * @return
	 */
	private void checkJsonParse(String json) {
		try {
			JSONObject.parse(json);
		} catch (Exception e) {
			// 尝试转换成 MAP 目前只有 {100011:1, 200001:2} 这样的配置可能用到这种解析
			this.checkJsonParseToMap(json);
		}
	}

	/**
	 * 验证JSON 字符串
	 * 
	 * @param json
	 * @return
	 */
	private void checkJsonParseToMap(String json) {
		try {
			JSONObject.parseObject(json, Map.class);
		} catch (Exception e) {
			throw new RuntimeException("JSON格式错误！");
		}
	}

	/**
	 * 验证定义的字段是否有大小写不同的重复值
	 * 
	 * @param map
	 */
	protected void checkFieldNameRepeat(Map<String, Map<String, String>> map) {
		List<String> list = new ArrayList<>();
		for (String key : map.keySet()) {
			String tmp = key.toLowerCase();

			if (list.contains(tmp)) {
				showMsgFront("===配置中存在重复字段:" + key);
			}
			list.add(key.toLowerCase());
		}
	}
	
	/**
	 * 获得文件路径,无后缀
	 * @param fileName 字段中的子表名
	 * @return
	 */
	public String getNewPath(String fileName){
		String ss = System.getProperty("user.dir");
		int index = ss.lastIndexOf("\\");
		ss = ss.substring(0, index+1);
		ss = ss+fileName;
		return ss;
	}
	
	/**
	 * 获得文件路径,带后缀
	 * @param path
	 * @return
	 */
	public String getRealPath(String path){
		String pathx = path+".xls";
		String pathxx = path + ".xlsx";
		String pathxxx = path + ".xlsm";
		File fx = new  File(pathx);
		File fxx = new  File(pathxx);
		File fxxx = new File(pathxxx);
		if(fx.isFile()){
			System.out.println("存在，路径："+fx.getPath());
			return pathx;
		}else if(fxx.isFile()){
			System.out.println("存在，路径："+fxx.getPath());
			return pathxx;
		}else if(fxxx.isFile()){
			System.out.println("存在，路径："+fxxx.getPath());
			return pathxxx;
		}else{
			System.out.println("非法路径");
			return "非法路径";
		}
		
	}
	
	/**
	 * 判断表格是否 包含子表
	 * @param sheet
	 * @param row
	 * @return
	 */
	public boolean hasSub(Sheet sheet){
		int columnNum = sheet.getRow(0).getLastCellNum();	
		boolean hasSub = false;
		for (int i = 0; i < columnNum; i++) {
			Cell cell = sheet.getRow(flagType).getCell(i);
			String type = cell.getStringCellValue().toLowerCase();
			if(type.equals(SUB)){
				hasSub = true;
			}
		}
		return hasSub;	
	}
	
	/**
	 * 得到包含子表的Excel文件名
	 * @param sheet
	 * @param tp
	 * @param cellRow
	 * @return
	 * @throws Exception 
	 */
	public String getSubName(Sheet sheet,String tp,int cellRow) throws Exception{
		int cellColumn = getColumByType(sheet, SUB);
		String re = getContentByCoordinate(sheet, cellRow, cellColumn);	
		return re;
	}
	
	/**
	 * 得到指定sn所在的行
	 * @param sheet
	 * @param sn
	 * @return
	 * @throws Exception
	 */
	public int getRowBySN(Sheet sheet,String sn) throws Exception{
		int maxRow = sheet.getLastRowNum();
		for (int i = flagNameCN; i < maxRow; i++) {
			Cell cell = sheet.getRow(i).getCell(0);
			String value = String.valueOf(this.processCellAndGetDate(sheet, cell, null));
			if(value.equals(sn)){
				return Integer.valueOf(i);
			}
		}
		return -1;
	}
	
	/**
	 * 得到指定类型所在的列
	 * @param sheet
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public int getColumByType(Sheet sheet,String type) {
		int maxColumn = sheet.getRow(0).getLastCellNum();//总列数
		for (int i = 0; i < maxColumn; i++) {
			Cell cell = sheet.getRow(flagType).getCell(i);
			String value = null;
			try {
				value = String.valueOf(this.processCellAndGetDate(sheet, cell, null));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(value.equals(type)){
				return Integer.valueOf(i);
			}
		}
		this.showMsgFront(sheet.getSheetName()+"===表格配置错误，没有"+ type);
		return -1;
	}
	
	/**
	 * 根据行列得到单元格内容
	 * @param sheet
	 * @param cellRow
	 * @param cellColumn
	 * @return
	 * @throws Exception
	 */
	public String getContentByCoordinate(Sheet sheet,int cellRow,int cellColumn) {
		Cell cell = sheet.getRow(cellRow).getCell(cellColumn);
		String value = null;
		try {
			value = String.valueOf(this.processCellAndGetDate(sheet, cell, null));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return value;
	}
	
	
	/**
	 * 表名和文件路径 {英文名=路径}，{Add=D\jar\addTest.xlsx}
	 * @param files
	 * @return
	 */
	public Map<String,String> filePathMap(File[] files) {
		Map<String,String> map = new HashMap<String,String>();
		Workbook workbook = null;
		
		for (int i = 0; i < files.length; i++) {
			String filePathName = files[i].getPath();	
			// 去掉../
			filePathName = this.processPath(filePathName);
			// 只处理 xls 和 xlsx的文件
			if (!filePathName.endsWith(".xls") && !filePathName.endsWith(".xlsx") && !filePathName.endsWith("xlsm")) {
				continue;
			}
			if (filePathName.indexOf("~$") != -1) {
				continue;
			}			
			try {
				workbook = WorkbookFactory.create(new FileInputStream(new File(filePathName)));
				int totalSheet = workbook.getNumberOfSheets();
				for (int j = 0; j < totalSheet; j++) {
					Sheet sheet = workbook.getSheetAt(j);
					// 表名
					String name = sheet.getSheetName();
					if (name.startsWith("Sheet"))
						continue;
					// 非法命名忽略
					if (!name.contains("|")) {
						continue;
					}

					// 表英文名
					String nameEN = name.substring(name.indexOf("|") + 1, name.length());
					if (map.get(nameEN) != null) {
						continue;
					}
					map.put(nameEN,filePathName);
				}
			} catch (InvalidFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return map;
	}
	
	/**
	 * 存放 表名--路径
	 * @param files
	 * @return
	 */
	public Map<String,String> filePathMap2(File[] files) {
		Map<String,String> map = new HashMap<String,String>();
		
		for (int i = 0; i < files.length; i++) {
			String filePathName = files[i].getPath();	
			
			// 去掉../
			filePathName = this.processPath(filePathName);
			// 只处理 xls 和 xlsx的文件
			if (!filePathName.endsWith(".xls") && !filePathName.endsWith(".xlsx") && !filePathName.endsWith("xlsm")) {
				continue;
			}
			if (filePathName.indexOf("~$") != -1) {
				continue;
			}	
			
			String name = getFileName(files[i].getName());			
			map.put(name, filePathName);			
		}
		return map;
	}
	
	/**
	 * 通过子表名获得子表所在的文件的路径
	 * @param name
	 * @return
	 */
	public String getPathByName(String name){		
		String re = nameMap.get(name);
		return re;		
	}
	
	/**
	 * 得到 母表所有的子表名字
	 * @param sheet
	 * @return
	 */
	public List<String> getAllSubFile(Sheet sheet) {
		List<String> subList = new ArrayList<String>();
		int cellColumn = getColumByType(sheet, SUB);//得到指定类型所在的列
		int rowNum = sheet.getLastRowNum();//总行数
		for (int i = 4; i < rowNum; i++) {
			String re = getContentByCoordinate(sheet, i, cellColumn);
			String[] strArr = strToArr(re);
			for (int j = 0; j < strArr.length; j++) {
				subList.add(strArr[j]);
			}			
		}
		return subList;
	}
	
	/**
	 * 把 String 分解成String[]
	 * @param str
	 * @return
	 */
	public  String[] strToArr(String str) {
		String[] strAry = null;
		if (str != null) {
			strAry = str.split(",");			
		}
		return strAry;
	}
	/**
	 * string 格式转换为 List<Integer>
	 * @param str
	 * @return
	 */
	public static List<Integer> strToIntList(String str) {
		if (str == null || str.isEmpty())
			return new ArrayList<Integer>();

		List<Integer> l = new ArrayList<Integer>();
		String[] o = str.split(",");
		for (String s : o) {
			if (s.isEmpty())
				continue;
			Integer.valueOf(s);
			if(isInteger(s)){
				l.add(Integer.valueOf(s));
			}
			
		}
		return l;
	}
	
	/**
	 * 字符串是否是数字
	 * @param value
	 * @return
	 */
	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * 通过表的英文名字 来获得 sheet对象
	 * @param workbook
	 * @param sub
	 * @return
	 */
	public Sheet getSheetByName(Workbook workbook,String sub){
		Sheet result = null;
		for (int i = 0;i<workbook.getNumberOfSheets();i++) {
			Sheet sh = workbook.getSheetAt(i);
			String name = sh.getSheetName();
			String nameEN = name.substring(name.indexOf("|") + 1, name.length());
			if(nameEN.equals(sub)){
				result = sh;
			}
		}
		return result;
	}
	
	/**
	 * 采集 子表数据
	 * @param sheet
	 * @param cellSub
	 * @param cellSubsn
	 * @return
	 */
	public Object getSubData(Sheet sheet,Cell cellSub,Cell cellSubsn) throws Exception{
		List<Object> listMap = new ArrayList<Object>();
		try {
			// 子表表名
			String[] subName = strToArr(cellSub.getStringCellValue());
			
			int type = cellSubsn.getCellType();
			String value = null;
			if(type == Cell.CELL_TYPE_STRING){
				value = cellSubsn.getStringCellValue();
			}else if(type == Cell.CELL_TYPE_NUMERIC){
				value = String.valueOf(formatSciNot(cellSubsn.getNumericCellValue()));
			}
			// 子表对应的sn
			String[] subSnList = strToArr(value);
			if(subName==null || subSnList==null || subName.length!=subSnList.length){
				this.showMsgFront("\n===子表与子表sn长度不一致：" + sheet.getSheetName());
				throw new Exception("子表与子表sn长度不一致");
			}
			for (int j = 0; j < subName.length; j++) {//遍历所有子表
				String name = subName[j];
				String subSn = subSnList[j];
				String subPath = getPathByName(name);
				Workbook workbook = WorkbookFactory.create(new FileInputStream(new File(subPath)));
				Sheet sh = null;
				switch (executeType) {
				case 1:
					sh = getSheetByName(workbook, name);
					break;
				case 2:
					sh = getSheetByName(workbook, this.language);
					break;
				default:
					break;
				}
				Object obj = getSheetData(sh,subSn);
				listMap.add(obj);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return listMap;
	}
	
	/**
	 * 采集表格数据
	 * 
	 * @param sheet
	 *            表格对象
	 * @param subsn
	 *            subsn对象，null表示母表
	 * @return
	 * @throws Exception
	 */
	public Object getSheetData(Sheet sheet, String subsn) throws Exception {

		List<Map<Object, Object>> resultsServer = new ArrayList<Map<Object, Object>>();
		Map<Object, Object> subMap = new HashMap<Object, Object>();
		// 导入数据的总行数
		int totalRow = sheet.getLastRowNum();
		// 没有数据
		if (totalRow == 0) {
			throw new Exception("===sheet：" + sheet.getSheetName() + ", 没有数据,请检查\n");
		}
		// 导入数据总列数
		int totalColume = sheet.getRow(0).getLastCellNum();

		for (int row = flagNameCN + 1; row <= totalRow; row++) {
			// 服务器端数据
			Map<Object, Object> serverMap = new LinkedHashMap<Object, Object>();
			// 客户端数据
			Map<Object, Object> clientMap = new LinkedHashMap<Object, Object>();
			Row ro = sheet.getRow(row);
			if (ro == null || ro.equals("")) {
				continue;
			}
			Cell cellSn = sheet.getRow(row).getCell(0);//sn 所在的cell
			String cellValue = String.valueOf(processCellAndGetDate(sheet, cellSn, "int"));
			boolean isServer = true;
			for (int colume = 0; colume < totalColume; colume++) { // 服务器或者客户端标识

				// 忽略没有定义 CS 头的列
				Cell cellSOrC = sheet.getRow(flagCS).getCell(colume);
				if (cellSOrC == null || cellSOrC.getCellType() == Cell.CELL_TYPE_BLANK) {
					continue;
				}

				String serverOrClient = cellSOrC.getStringCellValue().toLowerCase();

				// ***add by shenjh,排除一些异常	
				// 当前单元格
				Cell cell = sheet.getRow(row).getCell(colume);
				if (cell == null)
					continue;// 获取的格子为空
				if (cell.getCellType() == Cell.CELL_TYPE_BLANK)
					continue;// 获取的格子类型是空白的
				// ***end of add,排除一些异常

				// 数据类型
				String dateType;
				// 字段的英文名
				String fieldNameEN;
				// 当前列的数据类型
				dateType = sheet.getRow(flagType).getCell(colume).getStringCellValue();
				// 当前列的字段的英文名
				fieldNameEN = sheet.getRow(flagNameEN).getCell(colume).getStringCellValue();

				if (dateType.equals(SUBSN)) {
					continue;
				}
				

				// 第一列默认为ID(SN)，如果没有数值，此行忽略并扔出异常
				if (colume == 0) {
					if (subsn == null) {// 只记录母表的数据类型
						this.checkSnAndSaveType(cell, dateType);
					}
					// SN 统一大小写
					fieldNameEN = this.toSaveCase(fieldNameEN.toUpperCase());
				}	
				// 客户端数据
				if (serverOrClient.contains("c") && CSSign.equalsIgnoreCase("c")) {
					isServer = false;
					try {
						Object subObject = null;
						// 获得子表的数据
						if (dateType.equals(SUB)) {
							int col = getColumByType(sheet, SUBSN);
							Cell cell2 = sheet.getRow(row).getCell(col);// SUBSN的cell
							subObject = getSubData(sheet, cell, cell2);
							// 将数据按照不同类型放入map
							this.convertDataByTypeAndSetIntoMap(clientMap, fieldNameEN, dateType, subObject);
							continue;
						}
					} catch (Exception e) {
						throw new Exception("表头信息定义错误!");
					}
					
					// 获得最终数值
					Object value = this.processCellAndGetDate(sheet, cell, dateType);
					// 将数据按照不同类型放入map
					this.convertDataByTypeAndSetIntoMap(clientMap, fieldNameEN, dateType, value);
				}
				
				// 服务器端数据
				if (serverOrClient.contains("s") && CSSign.equalsIgnoreCase("s")) {
					try {
						Object subObject = null;
						// 获得子表的数据
						if (dateType.equals(SUB)) {
							int col = getColumByType(sheet, SUBSN);
							Cell cell2 = sheet.getRow(row).getCell(col);// SUBSN的cell
							subObject = getSubData(sheet, cell, cell2);
							// 将数据按照不同类型放入map
							this.convertDataByTypeAndSetIntoMap(serverMap, fieldNameEN, dateType, subObject);
							continue;
						}
					} catch (Exception e) {
						throw new Exception("表头信息定义错误!");
					}
					
					// 获得最终数值
					Object value = this.processCellAndGetDate(sheet, cell, dateType);
					// 将数据按照不同类型放入map
					this.convertDataByTypeAndSetIntoMap(serverMap, fieldNameEN, dateType, value);
				}
			}//for 列结束
			if (cellValue.equals(subsn)) {
				if(isServer){
					subMap = serverMap;
				}else{
					subMap = clientMap;
				}
				
			}
			if (!serverMap.isEmpty())
				resultsServer.add(serverMap);
			if (!clientMap.isEmpty())
				resultsClient.add(clientMap);
		}//for 行结束
		if (subsn != null) {//是子表 则返回子表数据
			return subMap;
		}
		return resultsServer;
	}
	
	
}
