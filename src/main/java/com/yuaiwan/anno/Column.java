package com.yuaiwan.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解类似与JPA中的注解,同时也模拟类似mybatis中的ResultMap,实现自定义结果集的返回
 * @author guangyu
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	String value() default "";//列名
	ColumType type() default ColumType.COLUMN;//列的类型(主键,平常列)
	SpecialColumn specialColum() default SpecialColumn.COLUMN;//特殊的列类型(删除标记,创建时间,修改时间,版本号);
	IncreaseType increaseType() default IncreaseType.MANUAL;
	String dataType();//数据类型长度(例:varchar(64))
	String defaultValue() default "";//默认值
	ColumnCanNull canNull() default ColumnCanNull.YES;//是否可以为null
	boolean unique() default false;//是否唯一
	String comment() default "";//备注
}
