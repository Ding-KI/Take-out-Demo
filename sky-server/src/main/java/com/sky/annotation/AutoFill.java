package com.sky.annotation;

/*
* Custom annotation, used to identify that a method needs to automatically fill the functional field*/ 

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // Specify database operation type: UPDATE, INSERT
    OperationType value();
}
