package com.kwic.makebill.utils;
/*
 *  
 *  커스텀 로그 생성해서 사용할경우 그클레스내에 나오게하거나 숨길수있다.
 *  Static이나 생성 사용할경우 이곳에서 보이기 안보이기 컨트롤가능 
 *  2013 .06 . 18 KMH
 *  
 */
import android.content.Context;
import android.util.Log;

public class CustomLog {
	static boolean isDebug =true;
	static String TAG  ="";
	boolean isClassDebug = false;
	public CustomLog(boolean isClassDebug , Context context) {
		this.isClassDebug= isClassDebug;
		CustomLog.TAG = context.getClass().getName();
	}

	public void dd(String params ) {
		if ( isClassDebug )
		{
			CustomLog.d(params);
		}
	}

	public void ee(String params) {
		if ( isClassDebug )
		{
			CustomLog.e( params);
		}
	}

	public void ii(String params) {
		if ( isClassDebug )
		{
			CustomLog.i( params);
		}
	}

	public void dd(String format ,Object... params) {
		if ( isClassDebug )
		{
			CustomLog.d(format, params);
		}
	}

	public void ee(String format ,Object... params) {
		if ( isClassDebug )
		{
			CustomLog.d(format, params);
		}
	}

	public void ii(String format ,Object... params) {
		if ( isClassDebug )
		{
			CustomLog.d(format, params);
		}
	}


	public static void d(String format ,Object... params)
	{
		if ( isDebug )
		{
			StackTraceElement[] stack= Thread.currentThread().getStackTrace();
			String fullClassName = stack[3].getClassName();            
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = stack[3].getMethodName();
			int lineNumber = stack[3].getLineNumber();
			TAG =className + "." + methodName + "():" + lineNumber;
			Log.d(TAG,className + "." + methodName + "():" + lineNumber   + "\n"  + String.format(format, params));
		}
	}

	public static void e(String format ,Object... params)
	{
		if ( isDebug )
		{	
			StackTraceElement[] stack= Thread.currentThread().getStackTrace();
			String fullClassName = stack[3].getClassName();            
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = stack[3].getMethodName();
			int lineNumber = stack[3].getLineNumber();
			TAG =className + "." + methodName + "():" + lineNumber;
			Log.e(TAG,className + "." + methodName + "():" + lineNumber   + "\n"  + String.format(format, params));
		}
	}

	public static void i(String format ,Object... params)
	{
		if ( isDebug )
		{	
			StackTraceElement[] stack= Thread.currentThread().getStackTrace();

			String fullClassName = stack[3].getClassName();            
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = stack[3].getMethodName();
			int lineNumber = stack[3].getLineNumber();
			TAG =className + "." + methodName + "():" + lineNumber;
			Log.d(TAG, String.format(format, params));
		}
	}


	public static void d(String params)
	{
		if ( isDebug )
		{	
			StackTraceElement[] stack= Thread.currentThread().getStackTrace();

			String fullClassName = stack[3].getClassName();            
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = stack[3].getMethodName();
			int lineNumber = stack[3].getLineNumber();
			TAG =className + "." + methodName + "():" + lineNumber;
			Log.d(TAG, params);
		}
	}

	public static void e(String params)
	{
		if ( isDebug )
		{	
			StackTraceElement[] stack= Thread.currentThread().getStackTrace();

			String fullClassName = stack[3].getClassName();            
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = stack[3].getMethodName();
			int lineNumber = stack[3].getLineNumber();
			TAG =className + "." + methodName + "():" + lineNumber;
			Log.e(TAG, params);

		}
	}

	public static void i(String params)
	{
		if ( isDebug )
		{	

			StackTraceElement[] stack= Thread.currentThread().getStackTrace();

			String fullClassName = stack[3].getClassName();            
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = stack[3].getMethodName();
			int lineNumber = stack[3].getLineNumber();
			TAG =className + "." + methodName + "():" + lineNumber;
			Log.i(TAG, params);
		}
	}


}
