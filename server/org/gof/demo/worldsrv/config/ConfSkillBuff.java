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
 * SkillBuff.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkillBuff {
	public final int sn;			//Buff sn
	public final String name;			//名称
	public final String[] buffEffects;			//持续特效Act
	public final String[] buffDeadEffects;			//buff消失ACT
	public final String effectOnce;			//单次特效Act
	public final String explain;			//文字说明
	public final int type;			//类型(可用于覆盖)
	public final String dispelSn;			//被驱散组SN
	public final int multiOne;			//不同来源叠加规则：叠加参数，0不叠加，1时间叠加，2属性叠加
	public final int multiTwo;			//同来源叠加规则：叠加参数，0不叠加，1时间叠加，2属性叠加
	public final int multiMax;			//对于属性叠加，最大的叠加层数
	public final int priority;			//优先级
	public final int msgShowType;			//传播范围，0：不给前端发消息；1：只给自己发；2：给所有能看见的玩家发
	public final boolean isReserveDied;			//死后是否保留
	public final boolean isReserveSwitch;			//是否过图保留
	public final boolean isReserveOffLine;			//下线是否保留
	public final boolean isPeriod;			//是否周期触发
	public final int timePeriod;			//周期触发时间
	public final int timeExistOnline;			// 在线作用时间（ms），Buff在玩家在线时的作用时间，为0则表示buff的移除不受该时间的影响
	public final int timeExist;			// 最大存在时间ms，就是指该buff最多存在的时间，无论玩家是否在线，为0则表示buff的移除不受该时间的影响
	public final int timeDecay;			//被攻击时候的衰减时间.只对timeExist>0的数据有效（毫秒）
	public final int timeDelay;			// 延迟时间，毫秒(ms)
	public final String[] propName;			//atkSpeed以1000
	public final float[] propValue;			//移动速度基础值：500（5m/s）；填加减值攻击速度基础值：1000，填加减值；其他为10000是100%
	public final float[] levelParam;			//升级参数， 技能等级 * 参数 +到前面的数值
	public final String defProp;			//自己的属性影响
	public final float defWeight;			//自己属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
	public final String fireProp;			//释放者的属性
	public final float fireWeight;			//释放者属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
	public final int param1;			//额外参数1
	public final boolean helpful;			//是否是有益技能

	public ConfSkillBuff(int sn, String name, String[] buffEffects, String[] buffDeadEffects, String effectOnce, String explain, int type, String dispelSn, int multiOne, int multiTwo, int multiMax, int priority, int msgShowType, boolean isReserveDied, boolean isReserveSwitch, boolean isReserveOffLine, boolean isPeriod, int timePeriod, int timeExistOnline, int timeExist, int timeDecay, int timeDelay, String[] propName, float[] propValue, float[] levelParam, String defProp, float defWeight, String fireProp, float fireWeight, int param1, boolean helpful) {
			this.sn = sn;		
			this.name = name;		
			this.buffEffects = buffEffects;		
			this.buffDeadEffects = buffDeadEffects;		
			this.effectOnce = effectOnce;		
			this.explain = explain;		
			this.type = type;		
			this.dispelSn = dispelSn;		
			this.multiOne = multiOne;		
			this.multiTwo = multiTwo;		
			this.multiMax = multiMax;		
			this.priority = priority;		
			this.msgShowType = msgShowType;		
			this.isReserveDied = isReserveDied;		
			this.isReserveSwitch = isReserveSwitch;		
			this.isReserveOffLine = isReserveOffLine;		
			this.isPeriod = isPeriod;		
			this.timePeriod = timePeriod;		
			this.timeExistOnline = timeExistOnline;		
			this.timeExist = timeExist;		
			this.timeDecay = timeDecay;		
			this.timeDelay = timeDelay;		
			this.propName = propName;		
			this.propValue = propValue;		
			this.levelParam = levelParam;		
			this.defProp = defProp;		
			this.defWeight = defWeight;		
			this.fireProp = fireProp;		
			this.fireWeight = fireWeight;		
			this.param1 = param1;		
			this.helpful = helpful;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfSkillBuff> findAll() {
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
	public static ConfSkillBuff get(Integer sn) {
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
	public static ConfSkillBuff getBy(Object...params) {
		List<ConfSkillBuff> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfSkillBuff> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkillBuff> utilBase(Object...params) {
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
		List<ConfSkillBuff> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkillBuff c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfSkillBuff>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfSkillBuff a, ConfSkillBuff b) {
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
		public static final String sn = "sn";	//Buff sn
		public static final String name = "name";	//名称
		public static final String buffEffects = "buffEffects";	//持续特效Act
		public static final String buffDeadEffects = "buffDeadEffects";	//buff消失ACT
		public static final String effectOnce = "effectOnce";	//单次特效Act
		public static final String explain = "explain";	//文字说明
		public static final String type = "type";	//类型(可用于覆盖)
		public static final String dispelSn = "dispelSn";	//被驱散组SN
		public static final String multiOne = "multiOne";	//不同来源叠加规则：叠加参数，0不叠加，1时间叠加，2属性叠加
		public static final String multiTwo = "multiTwo";	//同来源叠加规则：叠加参数，0不叠加，1时间叠加，2属性叠加
		public static final String multiMax = "multiMax";	//对于属性叠加，最大的叠加层数
		public static final String priority = "priority";	//优先级
		public static final String msgShowType = "msgShowType";	//传播范围，0：不给前端发消息；1：只给自己发；2：给所有能看见的玩家发
		public static final String isReserveDied = "isReserveDied";	//死后是否保留
		public static final String isReserveSwitch = "isReserveSwitch";	//是否过图保留
		public static final String isReserveOffLine = "isReserveOffLine";	//下线是否保留
		public static final String isPeriod = "isPeriod";	//是否周期触发
		public static final String timePeriod = "timePeriod";	//周期触发时间
		public static final String timeExistOnline = "timeExistOnline";	// 在线作用时间（ms），Buff在玩家在线时的作用时间，为0则表示buff的移除不受该时间的影响
		public static final String timeExist = "timeExist";	// 最大存在时间ms，就是指该buff最多存在的时间，无论玩家是否在线，为0则表示buff的移除不受该时间的影响
		public static final String timeDecay = "timeDecay";	//被攻击时候的衰减时间.只对timeExist>0的数据有效（毫秒）
		public static final String timeDelay = "timeDelay";	// 延迟时间，毫秒(ms)
		public static final String propName = "propName";	//atkSpeed以1000
		public static final String propValue = "propValue";	//移动速度基础值：500（5m/s）；填加减值攻击速度基础值：1000，填加减值；其他为10000是100%
		public static final String levelParam = "levelParam";	//升级参数， 技能等级 * 参数 +到前面的数值
		public static final String defProp = "defProp";	//自己的属性影响
		public static final String defWeight = "defWeight";	//自己属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
		public static final String fireProp = "fireProp";	//释放者的属性
		public static final String fireWeight = "fireWeight";	//释放者属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
		public static final String param1 = "param1";	//额外参数1
		public static final String helpful = "helpful";	//是否是有益技能
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfSkillBuff> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkillBuff> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkillBuff> getMap() {
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
			Map<Integer, ConfSkillBuff> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfSkillBuff object = new ConfSkillBuff(conf.getIntValue("sn"), conf.getString("name"), parseStringArray(conf.getString("buffEffects")), parseStringArray(conf.getString("buffDeadEffects")), 
				conf.getString("effectOnce"), conf.getString("explain"), conf.getIntValue("type"), conf.getString("dispelSn"), 
				conf.getIntValue("multiOne"), conf.getIntValue("multiTwo"), conf.getIntValue("multiMax"), conf.getIntValue("priority"), 
				conf.getIntValue("msgShowType"), conf.getBooleanValue("isReserveDied"), conf.getBooleanValue("isReserveSwitch"), conf.getBooleanValue("isReserveOffLine"), 
				conf.getBooleanValue("isPeriod"), conf.getIntValue("timePeriod"), conf.getIntValue("timeExistOnline"), conf.getIntValue("timeExist"), 
				conf.getIntValue("timeDecay"), conf.getIntValue("timeDelay"), parseStringArray(conf.getString("propName")), parseFloatArray(conf.getString("propValue")), 
				parseFloatArray(conf.getString("levelParam")), conf.getString("defProp"), conf.getFloatValue("defWeight"), conf.getString("fireProp"), 
				conf.getFloatValue("fireWeight"), conf.getIntValue("param1"), conf.getBooleanValue("helpful"));
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
				String baseBath = ConfSkillBuff.class.getResource("").getPath();
				File file = new File(baseBath + "json/ConfSkillBuff.json");
				
				if(!file.exists()) { //运行路径没有，到jar包外部找
					baseBath = baseBath.substring(6, baseBath.indexOf("libs"));//去掉前6个字符"file:/"，否则找不到
					baseBath += "classes/";
					file = new File(baseBath + "json/ConfSkillBuff.json");
					if(!file.exists()) { //JAR包外部没有，到JAR包内部找
    					String currentJarPath = URLDecoder.decode(ConfMap.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"); //获取当前Jar文件名
						JarFile currentJar = new java.util.jar.JarFile(currentJarPath);
						JarEntry dbEntry = currentJar.getJarEntry("org/gof/demo/worldsrv/config/" + "json/ConfSkillBuff.json");
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