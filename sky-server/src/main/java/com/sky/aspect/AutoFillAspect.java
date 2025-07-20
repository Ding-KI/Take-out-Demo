package com.sky.aspect;

/*
* Custom aspect class, implement public field automatic filling processing logic*/

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /*Pointcut*/ 
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /*in advance notification: before method execution*/    
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("start to fill public fields");

        // Get the current intercepted method parameter (database operation type)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // Get method signature
        AutoFill autoFill= signature.getMethod().getAnnotation(AutoFill.class); // Get annotation object on method
        OperationType operationType = autoFill.value(); // Get operation type
        // Get the current intercepted method parameter - entity object
        Object[] args = joinPoint.getArgs();
        if(args.length == 0){
            return;
        }

        Object entity = args[0]; // Assume the first parameter is the entity object to be filled

        // Prepare data to be assigned - current time, current user ID, etc.

        LocalDateTime now = LocalDateTime.now(); // Get current time
        Long currentId = BaseContext.getCurrentId(); // Get current user ID

        // Determine operation type, set value by reflection
        if(operationType == OperationType.INSERT){
            // Set value for 4 fields
            try{
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // Set value by reflection
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            }catch (Exception e){
                e.printStackTrace();
            }

        }else if(operationType == OperationType.UPDATE){
            // Set value for 2 fields
            try{
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // Set value by reflection
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
