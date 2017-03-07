package com.yuaiwan.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	String value() default "";
	String comment() default "";
	boolean checkTable() default true;
	TableAutoType[] auto() default {TableAutoType.AUTO_CREATETABLE,TableAutoType.AUTO_CREATECOLUM};//修改表的方式,默认自动增加表,自动增加列
}
