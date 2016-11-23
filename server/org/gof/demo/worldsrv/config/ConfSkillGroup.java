package org.gof.demo.worldsrv.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.gof.core.db.OrderBy;
import org.gof.core.support.ConfigJSON;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;

/**
 * zhCN
 * SkillGroup.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkillGroup {
	public final int sn;			//技能组SN
	public final int[] activeSkills;			//主动技能
	public final int[] passiveSkills;			//被动技能
	public final int defaultSkill;			//默认攻击技能
	public final int[] relSkill;			//羁绊技能
	public final int[] activatedSkills;			//被动激活的主动技能

	public ConfSkillGroup(int sn, int[] activeSkills, int[] passiveSkills, int defaultSkill, int[] relSkill, int[] activatedSkills) {
			this.sn = sn;		
			this.activeSkills = activeSkills;		
			this.passiveSkills = passiveSkills;		
			this.defaultSkill = defaultSkill;		
			this.relSkill = relSkill;		
			this.activatedSkills = activatedSkills;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfSkillGroup> findAll() {
		return DATA.getList();
	}

	/**
	 * 是否存在key=SN的数据
	 * @param sn
	 * @return
	 */
	public static boolean containsKey(Integer sn) {
		return DATA.getMap().containsKey(sn);
	}
	
	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfSkillGroup get(Integer sn) {
		if(DATA.getMap().containsKey(sn)) {
			return DATA.getMap().get(sn);
		} else {
			return null;
		}
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfSkillGroup getBy(Object...params) {
		List<ConfSkillGroup> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfSkillGroup> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkillGroup> utilBase(Object...params) {
		List<Object> settings = Utils.ofList(params);
		
		//查询参数
		final Map<String, Object> paramsFilter = new LinkedHashMap<>();		//过滤条件
		final Map<String, OrderBy> paramsOrder = new LinkedHashMap<>();		//排序规则
				
		//参数数量
		int len = settings.size();
		
		//参数必须成对出现
		if(len % 2 != 0) {
			throw new SysException("查询参数必须成对出现:query={}", settings);
		}
		
		//处理成对参数
		for(int i = 0; i < len; i += 2) {
			String key = (String)settings.get(i);
			Object val = settings.get(i + 1);
			
			//参数 排序规则
			if(val instanceof OrderBy) {
				paramsOrder.put(key, (OrderBy) val);
			} else {	//参数 过滤条件
				paramsFilter.put(key, val);
			}
		}
		
		//返回结果
		List<ConfSkillGroup> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkillGroup c : DATA.getList()) {
				//本行数据是否符合过滤条件
				boolean bingo = true;
				
				//判断过滤条件
				for(Entry<String, Object> p : paramsFilter.entrySet()) {
					Field field = c.getClass().getField(p.getKey());
					
					//实际结果
					Object valTrue = field.get(c);
					//期望结果
					Object valWish = p.getValue();
					
					//有不符合过滤条件的
					if(!valWish.equals(valTrue)) {
						bingo = false;
						break;
					}
				}
				
				//记录符合结果
				if(bingo) {
					result.add(c);
				}
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
		
		//对结果进行排序
		Collections.sort(result, new Comparator<ConfSkillGroup>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfSkillGroup a, ConfSkillGroup b) {
				try {
					for(Entry<String, OrderBy> e : paramsOrder.entrySet()) {
						//两方字段
						Field fa = a.getClass().getField(e.getKey());
						Field fb = b.getClass().getField(e.getKey());
						//两方字段值
						Comparable va = (Comparable) fa.get(a);
						Comparable vb = (Comparable) fb.get(b);
						
						//值排序结果
						int compareResult = va.compareTo(vb);
						
						//相等时 根据下一个值进行排序
						if(va.compareTo(vb) == 0) continue;
						
						//配置排序规则
						OrderBy order = e.getValue();
						if(order == OrderBy.ASC) return compareResult;		//正序
						else return -1 * compareResult;					//倒序
					}
				} catch (Exception e) {
					throw new SysException(e);
				}

				return 0;
			}
		});
		
		return result;
	}

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String sn = "sn";	//技能组SN
		public static final String activeSkills = "activeSkills";	//主动技能
		public static final String passiveSkills = "passiveSkills";	//被动技能
		public static final String defaultSkill = "defaultSkill";	//默认攻击技能
		public static final String relSkill = "relSkill";	//羁绊技能
		public static final String activatedSkills = "activatedSkills";	//被动激活的主动技能
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfSkillGroup> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkillGroup> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkillGroup> getMap() {
			//延迟初始化
			if(_map == null) {
				synchronized (DATA.class) {
					if(_map == null) {
						_init();
					}
				}
			}
			return _map;
		}

		/**
		 * 初始化数据
		 */
		private static void _init() {
			Map<Integer, ConfSkillGroup> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfSkillGroup object = new ConfSkillGroup(conf.getIntValue("sn"), parseIntArray(conf.getString("activeSkills")), parseIntArray(conf.getString("passiveSkills")), conf.getIntValue("defaultSkill"), 
				parseIntArray(conf.getString("relSkill")), parseIntArray(conf.getString("activatedSkills")));
				dataMap.put(conf.getInteger("sn"), object);
			}

			//保存数据
			_map = Collections.unmodifiableMap(dataMap);
		}
		
		public static double[] parseDoubleArray(String value) {
			if(value == null) 
				return null;
			String[] elems = value.split(",");
			if(elems.length > 0) {
				double []temp = new double[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.doubleValue(elems[i]);
				}
				return temp;
			}
			return null;
	  	}
	  
		public static float[] parseFloatArray(String value) {
			if(value == null) 
				return null;
			String[] elems = value.split(",");
			if(elems.length > 0) {
				float []temp = new float[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.floatValue(elems[i]);
				}
				return temp;
			}
			return null;
		}
		
		public static int[] parseIntArray(String value) {
			if(value == null) 
				return null;
			String[] elems = value.split(",");
			if(elems.length > 0) {
				int []temp = new int[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.intValue(elems[i]);
				}
				return temp;
			}
			return null;
		}
	
		public static String[] parseStringArray(String value) {
			if(value == null) 
				return null;
			String[] elems = value.split(",");
			if(elems.length > 0) {
				String []temp = new String[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = elems[i];
				}
				return temp;
			}
			return null;
		}
		
		public static long[] parseLongArray(String value) {
			if(value == null) 
				return null;
			String[] elems = value.split(",");
			if(elems.length > 0) {
				long []temp = new long[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.longValue(elems[i]);
				}
				return temp;
			}
			return null;
		}
		
	 	public static boolean[] parseBoolArray(String value) {
	 		if(value == null) 
				return null;
			String[] elems = value.split(",");
			if(elems.length > 0) {
				boolean []temp = new boolean[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.booleanValue(elems[i]);
				}
				return temp;
			}
			return null;
	 	}
	
		/**
		 * 读取游戏配置
		 */
		private static String _readConfFile() {
			String result = "";
			try {
				FileInputStream fis = null;
				InputStreamReader isr = null;
				String baseBath = ConfSkillGroup.class.getResource("").getPath();
				File file = new File(baseBath + "json/ConfSkillGroup.json");
				
				if(!file.exists()) { //运行路径没有，到jar包外部找
					baseBath = baseBath.substring(6, baseBath.indexOf("libs"));//去掉前6个字符"file:/"，否则找不到
					baseBath += "classes/";
					file = new File(baseBath + "json/ConfSkillGroup.json");
					if(!file.exists()) { //JAR包外部没有，到JAR包内部找
    					String currentJarPath = URLDecoder.decode(ConfMap.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"); //获取当前Jar文件名
						JarFile currentJar = new java.util.jar.JarFile(currentJarPath);
						JarEntry dbEntry = currentJar.getJarEntry("org/gof/demo/worldsrv/config/" + "json/ConfSkillGroup.json");
						InputStream is = currentJar.getInputStream(dbEntry);
						isr = new InputStreamReader(is, "UTF-8");
					} else {
						fis = new FileInputStream(file);
						isr = new InputStreamReader(fis, "UTF-8");
					}
				} else {
					fis = new FileInputStream(file);
					isr = new InputStreamReader(fis, "UTF-8");
				}
				
				BufferedReader reader = new BufferedReader(isr);
			    String tempString = "";
			    while ((tempString = reader.readLine()) != null) {
					result += tempString;
			    }
			} catch (IOException e) {
			    throw new RuntimeException(e);
			}
			return result;
		}
	}
    
}