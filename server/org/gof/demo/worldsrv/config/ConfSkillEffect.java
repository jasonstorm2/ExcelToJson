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
 * SkillEffect.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkillEffect {
	public final int sn;			//sn
	public final int sequence;			//排序号
	public final int period;			//两段式的哪段（0,1）填0的代表发招立刻后端发送给前端的逻辑，伤害基本都在这；填1的表示等待施法时间之后再发送，一般用于buff和dot
	public final int[] comboCount;			//三连击，连点对应的第几次连击（0,1,2…）
	public final double triggerPct;			//触发几率
	public final boolean inheritTarget;			//是否继承上一次的目标
	public final int scopeType;			//目标范围类型
	public final float scopeParam1;			//范围参数1
	public final float scopeParam2;			//范围参数2
	public final float scopeParam3;			//范围参数3
	public final int targetNum;			//范围搜索的目标个数
	public final int targetFriend;			//技能友好度，=0为中性技能，>0为正面技能，<0为 负面技能
	public final boolean targetSelf;			//目标是否有自己 单体对自己的技能 targetSelf 为true ,如果为false那么忽略
	public final boolean excludeTeamBundle;			//是否排除自己武将小弟 为true删除自己的全部人， 为false 删除敌人的全部人
	public final boolean attackMove;			//是否攻击追击
	public final float disAtk;			//攻击方移动距离
	public final float disDef;			//防守方移动距离
	public final int disDefType;			//防守方移动距离类型
	public final int disSpeed;			//移动速度
	public final int cost;			//升级消耗
	public final int levelParam;			//升级参数，如果伤害 那么是技能等级*参数， 如果buff 为概率， 大于等于对方等级的发生的概率
	public final int affectedObj;			//生效范围
	public final int logicSn;			//调用的逻辑库ID
	public final String[] param1;			//参数1
	public final String[] param2;			//参数2 对于Buff 来说是产生概率
	public final String[] param3;			//参数3
	public final String[] param4;			//参数4
	public final String[] param5;			//参数5
	public final String[] param6;			//参数6
	public final String[] param7;			//参数7
	public final String[] param8;			//参数8

	public ConfSkillEffect(int sn, int sequence, int period, int[] comboCount, double triggerPct, boolean inheritTarget, int scopeType, float scopeParam1, float scopeParam2, float scopeParam3, int targetNum, int targetFriend, boolean targetSelf, boolean excludeTeamBundle, boolean attackMove, float disAtk, float disDef, int disDefType, int disSpeed, int cost, int levelParam, int affectedObj, int logicSn, String[] param1, String[] param2, String[] param3, String[] param4, String[] param5, String[] param6, String[] param7, String[] param8) {
			this.sn = sn;		
			this.sequence = sequence;		
			this.period = period;		
			this.comboCount = comboCount;		
			this.triggerPct = triggerPct;		
			this.inheritTarget = inheritTarget;		
			this.scopeType = scopeType;		
			this.scopeParam1 = scopeParam1;		
			this.scopeParam2 = scopeParam2;		
			this.scopeParam3 = scopeParam3;		
			this.targetNum = targetNum;		
			this.targetFriend = targetFriend;		
			this.targetSelf = targetSelf;		
			this.excludeTeamBundle = excludeTeamBundle;		
			this.attackMove = attackMove;		
			this.disAtk = disAtk;		
			this.disDef = disDef;		
			this.disDefType = disDefType;		
			this.disSpeed = disSpeed;		
			this.cost = cost;		
			this.levelParam = levelParam;		
			this.affectedObj = affectedObj;		
			this.logicSn = logicSn;		
			this.param1 = param1;		
			this.param2 = param2;		
			this.param3 = param3;		
			this.param4 = param4;		
			this.param5 = param5;		
			this.param6 = param6;		
			this.param7 = param7;		
			this.param8 = param8;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfSkillEffect> findAll() {
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
	public static ConfSkillEffect get(Integer sn) {
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
	public static ConfSkillEffect getBy(Object...params) {
		List<ConfSkillEffect> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfSkillEffect> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkillEffect> utilBase(Object...params) {
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
		List<ConfSkillEffect> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkillEffect c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfSkillEffect>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfSkillEffect a, ConfSkillEffect b) {
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
		public static final String sn = "sn";	//sn
		public static final String sequence = "sequence";	//排序号
		public static final String period = "period";	//两段式的哪段（0,1）填0的代表发招立刻后端发送给前端的逻辑，伤害基本都在这；填1的表示等待施法时间之后再发送，一般用于buff和dot
		public static final String comboCount = "comboCount";	//三连击，连点对应的第几次连击（0,1,2…）
		public static final String triggerPct = "triggerPct";	//触发几率
		public static final String inheritTarget = "inheritTarget";	//是否继承上一次的目标
		public static final String scopeType = "scopeType";	//目标范围类型
		public static final String scopeParam1 = "scopeParam1";	//范围参数1
		public static final String scopeParam2 = "scopeParam2";	//范围参数2
		public static final String scopeParam3 = "scopeParam3";	//范围参数3
		public static final String targetNum = "targetNum";	//范围搜索的目标个数
		public static final String targetFriend = "targetFriend";	//技能友好度，=0为中性技能，>0为正面技能，<0为 负面技能
		public static final String targetSelf = "targetSelf";	//目标是否有自己 单体对自己的技能 targetSelf 为true ,如果为false那么忽略
		public static final String excludeTeamBundle = "excludeTeamBundle";	//是否排除自己武将小弟 为true删除自己的全部人， 为false 删除敌人的全部人
		public static final String attackMove = "attackMove";	//是否攻击追击
		public static final String disAtk = "disAtk";	//攻击方移动距离
		public static final String disDef = "disDef";	//防守方移动距离
		public static final String disDefType = "disDefType";	//防守方移动距离类型
		public static final String disSpeed = "disSpeed";	//移动速度
		public static final String cost = "cost";	//升级消耗
		public static final String levelParam = "levelParam";	//升级参数，如果伤害 那么是技能等级*参数， 如果buff 为概率， 大于等于对方等级的发生的概率
		public static final String affectedObj = "affectedObj";	//生效范围
		public static final String logicSn = "logicSn";	//调用的逻辑库ID
		public static final String param1 = "param1";	//参数1
		public static final String param2 = "param2";	//参数2 对于Buff 来说是产生概率
		public static final String param3 = "param3";	//参数3
		public static final String param4 = "param4";	//参数4
		public static final String param5 = "param5";	//参数5
		public static final String param6 = "param6";	//参数6
		public static final String param7 = "param7";	//参数7
		public static final String param8 = "param8";	//参数8
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfSkillEffect> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkillEffect> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkillEffect> getMap() {
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
			Map<Integer, ConfSkillEffect> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfSkillEffect object = new ConfSkillEffect(conf.getIntValue("sn"), conf.getIntValue("sequence"), conf.getIntValue("period"), parseIntArray(conf.getString("comboCount")), 
				conf.getDoubleValue("triggerPct"), conf.getBooleanValue("inheritTarget"), conf.getIntValue("scopeType"), conf.getFloatValue("scopeParam1"), 
				conf.getFloatValue("scopeParam2"), conf.getFloatValue("scopeParam3"), conf.getIntValue("targetNum"), conf.getIntValue("targetFriend"), 
				conf.getBooleanValue("targetSelf"), conf.getBooleanValue("excludeTeamBundle"), conf.getBooleanValue("attackMove"), conf.getFloatValue("disAtk"), 
				conf.getFloatValue("disDef"), conf.getIntValue("disDefType"), conf.getIntValue("disSpeed"), conf.getIntValue("cost"), 
				conf.getIntValue("levelParam"), conf.getIntValue("affectedObj"), conf.getIntValue("logicSn"), parseStringArray(conf.getString("param1")), 
				parseStringArray(conf.getString("param2")), parseStringArray(conf.getString("param3")), parseStringArray(conf.getString("param4")), parseStringArray(conf.getString("param5")), 
				parseStringArray(conf.getString("param6")), parseStringArray(conf.getString("param7")), parseStringArray(conf.getString("param8")));
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
				String baseBath = ConfSkillEffect.class.getResource("").getPath();
				File file = new File(baseBath + "json/ConfSkillEffect.json");
				
				if(!file.exists()) { //运行路径没有，到jar包外部找
					baseBath = baseBath.substring(6, baseBath.indexOf("libs"));//去掉前6个字符"file:/"，否则找不到
					baseBath += "classes/";
					file = new File(baseBath + "json/ConfSkillEffect.json");
					if(!file.exists()) { //JAR包外部没有，到JAR包内部找
    					String currentJarPath = URLDecoder.decode(ConfMap.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"); //获取当前Jar文件名
						JarFile currentJar = new java.util.jar.JarFile(currentJarPath);
						JarEntry dbEntry = currentJar.getJarEntry("org/gof/demo/worldsrv/config/" + "json/ConfSkillEffect.json");
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