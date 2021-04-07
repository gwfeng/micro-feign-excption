package com.parent.common.exception;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.parent.common.exception.ResultStatus.ERROR_ARGS;

/**
 * ErrorValidation
 *
 * @author Chensong
 * @date 2017/11/17
 */
public class ErrorValidationNew {

    public static Result getInstance(MethodArgumentNotValidException ex) {
        List<Field> error = new ArrayList<>();
        // requestBody对象的Class
        Class clazz = ex.getParameter().getParameterType();
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String msg = fieldError.getDefaultMessage();
            Type type = new Type(fieldError, clazz);
            error.add(new Field(field, type, msg));
        }
        return new Result(ERROR_ARGS, error);
    }

    public static Result getInstance(ConstraintViolationException ex) {
        List<Field> error = new ArrayList<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String field = ((PathImpl) violation.getPropertyPath()).getLeafNode().asString();
            String msg = violation.getMessage();
            Type type = new Type(violation.getConstraintDescriptor());
            error.add(new Field(field, type, msg));
        }
        return new Result(ERROR_ARGS, error);
    }

    public static Result getInstance(ServletRequestBindingException ex) {
        return new Result(ERROR_ARGS, ex.getLocalizedMessage());
    }

    public static Result getInstance(MissingServletRequestParameterException ex) {
        return new Result(ERROR_ARGS, ex.getLocalizedMessage());
    }

    public static Result getInstance(BindException ex) {
        List<Field> error = new ArrayList<>();
        List<FieldError> fieldErrors = ex.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String msg = fieldError.getDefaultMessage();
            Type type = new Type(fieldError, ex.getTarget().getClass());
            error.add(new Field(field, type, msg));
        }
        return new Result(ERROR_ARGS, error);
    }

    public static Result getInstance(HttpMessageNotReadableException ex) {
        return new Result(ERROR_ARGS, ex.getLocalizedMessage());
    }

    public static Result getInstance(MethodArgumentTypeMismatchException ex) {
        return new Result(ERROR_ARGS, new Format(ex.getName(), ex.getValue(), ex.getRequiredType(), ex.getLocalizedMessage()));
    }

    @Data
    @AllArgsConstructor
    static class Format {
        private String name;
        private Object value;
        private Class requiredType;
        private String msg;
    }

    static class Field {
        private String name;

        private Type type;

        private String msg;

        public Field(String name, Type type, String msg) {
            this.name = name;
            this.type = type;
            this.msg = msg;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public String getMsg() {
            return msg;
        }
    }

    static class Type {

        private String anno;
        private Map<String, Object> attrs;

        public Type(FieldError fieldError, Class clazz) {
            this.anno = fieldError.getCode();
            try {
                for (Annotation anno : clazz.getDeclaredField(fieldError.getField()).getAnnotations()) {
                    Class annoType = anno.annotationType();
                    if (annoType.getSimpleName().equals(this.anno)) {
                        Map<String, Object> attrs = Maps.newHashMap();
                        for (Method method : annoType.getDeclaredMethods()) {
                            String annoField = method.getName();
                            attrs.put(annoField, method.invoke(anno));
                        }
                        this.attrs = Collections.unmodifiableMap(attrs);
                        break;
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public Type(String anno, Map<String, Object> attrs) {
            this.anno = anno;
            this.attrs = attrs;
        }

        public Type(ConstraintDescriptor constraintDescriptor) {
            this(constraintDescriptor.getAnnotation().annotationType().getSimpleName(), constraintDescriptor.getAttributes());
        }

        public String getAnno() {
            return anno;
        }

        public Map<String, Object> getAttrs() {
            return attrs;
        }
    }
}
