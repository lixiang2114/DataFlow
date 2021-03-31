package com.github.lixiang2114.flow.util;

import java.util.Calendar;
import java.util.HashMap;

/**
 * @author Lixiang
 * 时间解析器工具
 */
public class DateUtil {
    /**
     * 时间格式表
     */
    private static final java.util.List<String> FORMAT_LIST = java.util.Arrays.asList("yyyy","MM","dd","HH","mm","ss","S");

    /**
     * 时间解析正则式
     */
    private static final java.util.regex.Pattern DATE_REGEX = java.util.regex.Pattern.compile("(\\d{4})?-?([01]\\d{1})?(?!:)-?([0123]\\d{1})?(?!:)\\s*([012]\\d{1})?:?([012345]\\d{1})?:?([012345]\\d{1})?\\s*(\\d{1,3})?");
	
    /**
     * 获取简单时间格式字符串
     * @return 日期格式串
     */
    public static String getSimpleDateFormatStr(){
    	return "yyyy-MM-dd HH:mm:ss S";
    }
    
    /**
     * 获取默认时间格式
     * @return 格式化对象
     */
    public static java.text.DateFormat getDefaultDateFormat(){
    	return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * 日历类型转字串类型
     * @param calendar 日历类型
     * @return 字串类型
     */
    public static String calendarToString(java.util.Calendar calendar){
    	String dateStr=new java.sql.Timestamp(calendar.getTimeInMillis()).toString();
    	int endIndex=dateStr.indexOf(".");
    	return dateStr.substring(0, -1==endIndex?dateStr.length():endIndex).trim();
    }
    
    /**
     * 日历类型转长整型(格林威治毫秒数)
     * @param calendar 日历类型
     * @return 长整型
     */
    public static long calendarToMillSeconds(java.util.Calendar calendar){
    	return calendar.getTimeInMillis();
    }
    
    /**
     * 日历类型转日期类型
     * @param calendar 日历类型
     * @return 日期类型
     */
    public static java.util.Date calendarToDate(java.util.Calendar calendar){
    	return new java.util.Date(calendar.getTimeInMillis());
    }
    
    /**
     * 日历类型转日期类型
     * @param calendar 日历类型
     * @return 日期类型
     */
    public static java.sql.Date calendarToSqlDate(java.util.Calendar calendar){
    	return new java.sql.Date(calendar.getTimeInMillis());
    }
    
    /**
     * 日历类型转日期类型
     * @param calendar 日历类型
     * @return 日期类型
     */
    public static java.sql.Time calendarToTime(java.util.Calendar calendar){
    	return new java.sql.Time(calendar.getTimeInMillis());
    }
    
    /**
     * 日历类型转日期类型
     * @param calendar 日历类型
     * @return 日期类型
     */
    public static java.sql.Timestamp calendarToTimestamp(java.util.Calendar calendar){
    	return new java.sql.Timestamp(calendar.getTimeInMillis());
    }
    
    /**
     * 日期类型转日历类型
     * @param date 日期类型
     * @return 日历类型
     */
    public static java.util.Calendar dateToCalendar(java.util.Date date){
    	java.util.Calendar calendar=java.util.Calendar.getInstance();
    	calendar.setTimeInMillis(date.getTime());
    	return calendar;
    }
    
    /**
     * 长整型(格林威治毫秒数)转日历类型
     * @param millSeconds 长整型日期
     * @return 日历类型
     */
    public static java.util.Calendar millSecondsToCalendar(long millSeconds){
    	java.util.Calendar calendar=java.util.Calendar.getInstance();
    	calendar.setTimeInMillis(millSeconds);
    	return calendar;
    }
    
    /**
     * 字串型(格林威治毫秒数)转日历类型
     * @param dateString 字串类型日期
     * @return 日历类型
     */
    public static java.util.Calendar stringToCalendar(String dateString){
    	java.util.Calendar calendar=java.util.Calendar.getInstance();
    	calendar.setTimeInMillis(stringToDate(dateString,java.util.Date.class).getTime());
    	return calendar;
    }
    
    /**
     * 判断给定的参数日期是否是昨天
     * @param calendar 日历对象
     * @return 字段值列表
     */
    public static boolean isYesterday(Calendar calendar){
    	calendar.add(Calendar.DAY_OF_MONTH, 1);
    	return isToday(calendar);
    }
    
    /**
     * 判断给定的参数日期是否是明天
     * @param calendar 日历对象
     * @return 字段值列表
     */
    public static boolean isTomorrow(Calendar calendar){
    	calendar.add(Calendar.DAY_OF_MONTH, -1);
    	return isToday(calendar);
    }
    
    /**
     * 判断给定的参数日期是否是今天
     * @param calendar 日历对象
     * @return 字段值列表
     */
    public static boolean isToday(Calendar calendar){
    	Calendar today=Calendar.getInstance();
    	if(today.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) return false;
    	if(today.get(Calendar.MONTH)+1 != calendar.get(Calendar.MONTH)+1) return false;
    	if(today.get(Calendar.DATE) != calendar.get(Calendar.DATE)) return false;
    	return true;
    }
    
    /**
     * 获取Calendar常见字段值
     * @param calendar 日历对象
     * @return 字段值列表
     */
    public static Object getCalendarFields(Calendar calendar,Class<?> componentType){
    	int year=calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH)+1;
		int day=calendar.get(Calendar.DATE);
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		int minute=calendar.get(Calendar.MINUTE);
		int second=calendar.get(Calendar.SECOND);
		int millSecond=calendar.get(Calendar.MILLISECOND);
		int week=calendar.get(Calendar.DAY_OF_WEEK)-1;
		int dayOfYear=calendar.get(Calendar.DAY_OF_YEAR);
		int weekOfMonth=calendar.get(Calendar.WEEK_OF_MONTH);
		int weekOfYear=calendar.get(Calendar.WEEK_OF_YEAR);
		
		if(componentType.isPrimitive()){
			return new int[]{year,month,day,hour,minute,second,millSecond,week,dayOfYear,weekOfMonth,weekOfYear};
		}else{
			return new Integer[]{year,month,day,hour,minute,second,millSecond,week,dayOfYear,weekOfMonth,weekOfYear};
		}
    }
    
    /**
     * 获取Calendar常见字段值
     * @param calendar 日历字典
     * @return 字段值字典
     */
    public static HashMap<String,Integer> getCalendarFields(Calendar calendar){
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		map.put("year", calendar.get(Calendar.YEAR));
		map.put("month", calendar.get(Calendar.MONTH)+1);
		map.put("day", calendar.get(Calendar.DATE));
		map.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
		map.put("minute", calendar.get(Calendar.MINUTE));
		map.put("second", calendar.get(Calendar.SECOND));
		map.put("millSecond", calendar.get(Calendar.MILLISECOND));
		map.put("week", calendar.get(Calendar.DAY_OF_WEEK)-1);
		map.put("dayOfYear", calendar.get(Calendar.DAY_OF_YEAR));
		map.put("weekOfMonth", calendar.get(Calendar.WEEK_OF_MONTH));
		map.put("weekOfYear", calendar.get(Calendar.WEEK_OF_YEAR));
    	return map;
    }
    
    /**
     * String类型转java.util.Date类型
     * @param dateString 字串日期
     * @return java.util.Date类型
     */
    public static java.util.Date stringToDate(String dateString){
    	return (java.util.Date)stringToDate(dateString,java.util.Date.class);
    }
    
    /**
     * String类型转java.sql.Date类型
     * @param dateString 字串日期
     * @return java.sql.Date类型
     */
    public static java.sql.Date stringToSqlDate(String dateString){
    	return (java.sql.Date)stringToDate(dateString,java.sql.Date.class);
    }
    
    /**
     * String类型转java.sql.Time类型
     * @param dateString 字串日期
     * @return java.sql.Time类型
     */
    public static java.sql.Time stringToTime(String dateString){
    	return (java.sql.Time)stringToDate(dateString,java.sql.Time.class);
    }
    
    /**
     * String类型转java.sql.Timestamp类型
     * @param dateString 字串日期
     * @return java.sql.Timestamp类型
     */
    public static java.sql.Timestamp stringToTimestamp(String dateString){
    	return (java.sql.Timestamp)stringToDate(dateString,java.sql.Timestamp.class);
    }
    
    /**
     * 长整型(格林威治毫秒数)转java.util.Date类型
     * @param millSeconds 长整型日期
     * @return java.util.Date类型
     */
    public static java.util.Date millSecondsToDate(long millSeconds){
    	return (java.util.Date)millSecondsToDate(millSeconds,java.util.Date.class);
    }
    
    /**
     * 长整型(格林威治毫秒数)转java.sql.Date类型
     * @param millSeconds 长整型日期
     * @return java.sql.Date类型
     */
    public static java.sql.Date millSecondsToSqlDate(long millSeconds){
    	return (java.sql.Date)millSecondsToDate(millSeconds,java.sql.Date.class);
    }
    
    /**
     * 长整型(格林威治毫秒数)转java.sql.Time类型
     * @param millSeconds 长整型日期
     * @return java.sql.Time类型
     */
    public static java.sql.Time millSecondsToTime(long millSeconds){
    	return (java.sql.Time)millSecondsToDate(millSeconds,java.sql.Time.class);
    }
    
    /**
     * 长整型(格林威治毫秒数)转java.sql.Timestamp类型
     * @param millSeconds 长整型日期
     * @return java.sql.Timestamp类型
     */
    public static java.sql.Timestamp millSecondsToTimestamp(long millSeconds){
    	return (java.sql.Timestamp)millSecondsToDate(millSeconds,java.sql.Timestamp.class);
    }
    
    
    /**
     * 长整型(格林威治毫秒数)转字串类型
     * @param millSeconds 长整型日期
     * @return 字串类型
     */
    public static String millSecondsToString(long millSeconds){
    	String dateStr=new java.sql.Timestamp(millSeconds).toString();
    	int endIndex=dateStr.indexOf(".");
    	return dateStr.substring(0, -1==endIndex?dateStr.length():endIndex).trim();
    }
    
    /**
     * 时间类型转长整型(格林威治毫秒数)
     * @param date 时间类型日期
     * @return 长整型
     */
    public static long dateToMillSeconds(java.util.Date date){
    	return date.getTime();
    }
    
    /**
     * 时间类型转字串类型
     * @param date 时间类型日期
     * @return 字串类型
     */
    public static String dateToString(java.util.Date date){
    	String dateStr=new java.sql.Timestamp(date.getTime()).toString();
    	int endIndex=dateStr.indexOf(".");
    	return dateStr.substring(0, -1==endIndex?dateStr.length():endIndex).trim();
    }
    
    /**
     * 字串类型转长整型(格林威治毫秒数)
     * @param dateString 字串类型日期
     * @return 长整型
     */
    public static long stringToMillSeconds(String dateString){
    	return stringToDate(dateString,java.sql.Timestamp.class).getTime();
    }
    
    /**
     * 长整型(格林威治毫秒数)转字串类型
     * @param millSeconds 长整型日期
     * @return 字串类型
     */
    public static java.util.Date millSecondsToDate(long millSeconds,Class<? extends java.util.Date> dateType){
    	try {
			return dateType.getConstructor(long.class).newInstance(millSeconds);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * 字串类型转时间类型
     * @param dateString 字串类型日期
     * @return 时间类型
     */
    public static java.util.Date stringToDate(String dateString,Class<? extends java.util.Date> dateType){
    	try {
			return dateType.getConstructor(long.class).newInstance(getDateFormat(dateString).parse(dateString).getTime());
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * 获取时间格式化工具
     * @param dateString 时间字串
     * @return 时间格式对象
     */
    public static java.text.DateFormat getDateFormat(String dateString){
        return new java.text.SimpleDateFormat(getDateFormatStr(dateString));
    }
    
	/**
     * 获取时间格式字符串
     * @param dateString 时间字串
     * @return 时间格式字串
     */
    public static String getDateFormatStr(String dateString){
        if(null==dateString||dateString.trim().isEmpty())return null;
        java.util.regex.Matcher matcher=DATE_REGEX.matcher(dateString);
        if(!matcher.find()) return null;

        Integer groupCount=matcher.groupCount();
        StringBuilder formatBuilder=new StringBuilder();
        for(int i=1;i<=groupCount;i++){
            String curMatch=matcher.group(i);
            if(null==curMatch) continue;
            String format=FORMAT_LIST.get(i-1);
            if(i<3){
                formatBuilder.append(format).append("-");
                continue;
            }
            if(i==3){
                formatBuilder.append(format).append(" ");
                continue;
            }
            if(i<6){
                formatBuilder.append(format).append(":");
                continue;
            }
            if(i==6){
                formatBuilder.append(format).append(" ");
                continue;
            }
            formatBuilder.append(format).append(" ");
        }

        char lastChar=formatBuilder.charAt(formatBuilder.length()-1);
        if('-'==lastChar||':'==lastChar||' '==lastChar)formatBuilder.deleteCharAt(formatBuilder.length()-1);
        return formatBuilder.toString().trim();
    }
}
