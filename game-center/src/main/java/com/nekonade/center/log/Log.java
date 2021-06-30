package com.nekonade.center.log;

import java.lang.annotation.*;


/**
 *
 *  @Target({ElementType.PARAMETER, ElementType.METHOD})
 *  作用：用于描述注解的使用范围（即：被描述的注解可以用在什么地方）
 *
 * 　　取值(ElementType)有：
 *
 * 　　　　1.CONSTRUCTOR:用于描述构造器
 * 　　　　2.FIELD:用于描述域
 * 　　　　3.LOCAL_VARIABLE:用于描述局部变量
 * 　　　　4.METHOD:用于描述方法
 * 　　　　5.PACKAGE:用于描述包
 * 　　　　6.PARAMETER:用于描述参数
 * 　　　　7.TYPE:用于描述类、接口(包括注解类型) 或enum声明
 *
 * @Retention(RetentionPolicy.RUNTIME)  解释：@Retention()  定义了该Annotation（注释）被保留的时间长短
 *　取值（RetentionPoicy）有：
 *
 * 　　　　1.SOURCE:在源文件中有效（即源文件保留）
 * 　　　　2.CLASS:在class文件中有效（即class保留）
 * 　　　　3.RUNTIME:在运行时有效（即运行时保留）
 *
 *
 * 自定义注解： @interface  自定义注解
 *
 * 　定义注解格式：
 * 　　public @interface 注解名 {定义体}
 *
 * 　　注解参数的可支持数据类型：
 *
 * 　　　　1.所有基本数据类型（int,float,boolean,byte,double,char,long,short)
 * 　　　　2.String类型
 * 　　　　3.Class类型
 * 　　　　4.enum类型
 * 　　　　5.Annotation类型
 * 　　　　6.以上所有类型的数组
 *
 * 　　Annotation（注释）类型里面的参数该怎么设定:
 * 　　第一,只能用public或默认(default)这两个访问权修饰.例如,String value();这里把方法设为defaul默认类型；　 　
 * 　　第二,参数成员只能用基本类型byte,short,char,int,long,float,double,boolean八种基本数据类型和 String,Enum,Class,annotations等数据类型,以及这一些类型的数组.例如,String value();这里的参数成员就为String;　　
 * 　　第三,如果只有一个参数成员,最好把参数名称设为"value",后加小括号.例:下面的例子FruitName注解就只有一个参数成员。
 *
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 操作类型 **/
    String operateType();
    /** 操作解释 **/
    String operateExplain();
}
