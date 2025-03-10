package com.sky.aspect;

import com.sky.annotayion.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..))&&@annotation(com.sky.annotayion.AutoFill)")
    public void autoFillPoinCut(){}

    /**
     * 前置通知，通知中执行公共字段的赋值
     * @param joinPoint
     */
    @Before("autoFillPoinCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充……");
        ///获取到当前被拦截方法上的数据库操作类型
        MethodSignature  signature = (MethodSignature) joinPoint.getSignature();
        AutoFill  autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //获取到当前被拦截方法的参数
        Object[] args=joinPoint.getArgs();
        if(args==null||args.length==0){
            return;
        }

        Object entity=args[0];

        //准备赋值的数据
        LocalDateTime time=LocalDateTime.now();
        long empId= BaseContext.getCurrentId();

        //根据不同操作类型，为对应的属性通过反射赋值
        if(operationType.equals(OperationType.INSERT)){
            try{
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
               //通过反射调用目标对象的方法
                setCreateTime.invoke(entity, time);
                setUpdateTime.invoke(entity, time);
                setCreateUser.invoke(entity, empId);
                setUpdateUser.invoke(entity, empId);
    } catch (Exception ex) {
        log.error("公共字段自动填充失败：{}", ex.getMessage());
    }
            }
        else if(operationType.equals(OperationType.UPDATE)){}
    }


}
