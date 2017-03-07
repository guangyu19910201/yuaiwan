package com.yuaiwan.datasource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 此类实现了两个职责（为了减少类的数量将两个功能合并到一起了）：
 *   读/写动态数据库选择处理器
 *   通过AOP切面实现读/写选择
 *   
 * ★★读/写动态数据库选择处理器★★
 * 1、首先读取<tx:advice>事务属性配置
 * 2、对于所有读方法设置 read-only="true" 表示读取操作（以此来判断是选择读还是写库），其他操作都是走写库
 *    如<tx:method name="×××" read-only="true"/>
 * 3、 forceChoiceReadOnWrite用于确定在如果目前是写（即开启了事务），下一步如果是读，
 *    是直接参与到写库进行读，还是强制从读库读<br/>
 *      forceChoiceReadOnWrite:false 表示目前是写，下一步如果是读，强制参与到写事务（即从写库读）
 *                                  这样可以避免写的时候从读库读不到数据
 *                                  通过设置事务传播行为：SUPPORTS实现
 *      forceChoiceReadOnWrite:true 表示不管当前事务是写/读，都强制从读库获取数据
 *                                  通过设置事务传播行为：NOT_SUPPORTS实现（连接是尽快释放）                
 *                                  『此处借助了 NOT_SUPPORTS会挂起之前的事务进行操作 然后再恢复之前事务完成的』
 * 4、配置方式
 *  <bean id="dynamicDataSourceTransactionProcessor" class="com.yuaiwan.DynamicDataSourceProcessor">
 *     <property name="forceChoiceReadWhenWrite" value="false"/>
 *  </bean>
 * 5、目前只适用于<tx:advice>情况  支持@Transactional注解事务
 * ★★通过AOP切面实现读/写库选择★★
 * 
 * 1、首先将当前方法 与 根据之前【读/写动态数据库选择处理器】  提取的读库方法 进行匹配
 * 2、如果匹配，说明是读取数据：
 *  2.1、如果forceChoiceReadOnWrite:true，即在写操作的时候,如果执行读,强制走读库
 *  2.2、如果之前是写操作且forceChoiceReadOnWrite:false，在写操作的时候,如果执行读将从读库进行读取
 * 3、如果不匹配，说明默认将使用写库进行操作
 * 4、配置方式
 *      <aop:aspect order="-2147483648" ref="dynamicDataSourceTransactionProcessor">
 *          <aop:around pointcut-ref="txPointcut" method="determineReadOrWriteDB"/>
 *      </aop:aspect>
 *  4.1、此处order = Integer.MIN_VALUE 即最高的优先级（请参考http://jinnianshilongnian.iteye.com/blog/1423489）
 *  4.2、切入点：txPointcut 和 实施事务的切入点一样
 *  4.3、determineReadOrWriteDB方法用于决策是走读/写库的
 * @author guangyu
 */
public class DynamicDataSourceProcessor implements BeanPostProcessor {
	
	private enum MethodType {
        write, //写方法
        read; //读方法
    }
    //默认是false
    private boolean forceChoiceReadWhenWrite = false;
    
    //<方法表达式,是否走写库,true写,false读>
    private Map<String, MethodType> methodMap = new HashMap<String, MethodType>();

    /**
     * 当之前操作是写的时候，是否强制从从库读
     * 默认（false） 当之前操作是写，默认强制从写库读
     * @param forceReadOnWrite
     */
    public void setForceChoiceReadWhenWrite(boolean forceChoiceReadWhenWrite) {
        this.forceChoiceReadWhenWrite = forceChoiceReadWhenWrite;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(!(bean instanceof NameMatchTransactionAttributeSource)) {
            return bean;
        }
        try {
            NameMatchTransactionAttributeSource transactionAttributeSource = (NameMatchTransactionAttributeSource)bean;
            Field nameMapField = ReflectionUtils.findField(NameMatchTransactionAttributeSource.class, "nameMap");
            nameMapField.setAccessible(true);
            Map<String, TransactionAttribute> nameMap = (Map<String, TransactionAttribute>) nameMapField.get(transactionAttributeSource);
            for(Entry<String, TransactionAttribute> entry : nameMap.entrySet()) {
                RuleBasedTransactionAttribute attr = (RuleBasedTransactionAttribute)entry.getValue();
                String methodName = entry.getKey();
                //仅对read-only的处理
                if(attr.isReadOnly()) {
                    if(forceChoiceReadWhenWrite) {
                        //不管之前操作是写还是读，都默认强制从读库读 （设置为NOT_SUPPORTED即可）
                        attr.setPropagationBehavior(Propagation.NOT_SUPPORTED.value());
                    } else {//否则 设置为SUPPORTS（这样可以参与到写事务）
                        attr.setPropagationBehavior(Propagation.SUPPORTS.value());
                    }
                    methodMap.put(methodName, MethodType.read);
                }else{
                	methodMap.put(methodName, MethodType.write);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("process read/write transaction error", e);
        }
        return bean;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object determineReadOrWriteDB(ProceedingJoinPoint pjp) throws Throwable {
        if (isChoiceReadDB(pjp.getSignature().getName())) {
            DynamicDataSourceDecision.markRead();
        } else {
            DynamicDataSourceDecision.markWrite();
        }
        try {
            return pjp.proceed();
        } finally {
            DynamicDataSourceDecision.reset();
        }
    }
    
    private boolean isChoiceReadDB(String methodName) {
    	for (String mappedName : methodMap.keySet()) {
    		if (isMatch(methodName, mappedName)) {
    			MethodType methodType = methodMap.get(mappedName);
        		if(methodType == MethodType.write){//写方法
        			return false;
        		}else if(methodType == MethodType.read){//读方法
        			if(forceChoiceReadWhenWrite){//在写的过程中遇到读方法时强制选择读库
        				return true;
        			}else{
        				//如果之前选择了写库 现在还选择 写库
        		        if(DynamicDataSourceDecision.isChoiceWrite()) {
        		            return false;
        		        }else{
        		        	return true;
        		        }
        			}
        		}else{
        			//默认选择写库
        	        return false;
        		}
    		}
		}
    	//默认选择写库
        return false;
    }

    protected boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }
}

