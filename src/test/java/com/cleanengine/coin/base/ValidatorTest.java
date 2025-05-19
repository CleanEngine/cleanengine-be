package com.cleanengine.coin.base;

import com.cleanengine.coin.common.validation.ValidationHelper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// T는 검증 대상 클래스
public abstract class ValidatorTest<T> {
    protected static Validator validator;

    @BeforeAll
    public static void init(){
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * @param validateTarget Validation의 대상이 되는 instance를 전달한다.
     *                       javax.validation의 annotation으로 Constraint가 정의되어 있어야 한다.
     * @return Constraint를 위반한 field명을 key로, Constraint 위반 정보를 담는 ConstraintViolationInfo 인스턴스를 Value로 삼는 Map을 반환한다
     */
    protected <T> List<ConstraintViolationInfo> validate(T validateTarget) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(validateTarget);

        return constraintViolations.stream()
                .map(ConstraintViolationInfo::new)
                .collect(Collectors.toList());
    }


    /**
     * javax.validation의 ConstraintViolation 클래스로부터, Validation 검증에 필수적인 필드만을 추출해낸 클래스이다.
     */
    protected static class ConstraintViolationInfo{
        // 어떤 Cosntraint를 위반했는지 알기 위함
        Class constraintClass;
        // Constraint를 위반한 필드명
        String fieldName;
        // Constraint를 위반한 Value
        Object invalidValue;
        // Constraint를 위반하여 생성된 메시지
        String message;

        public ConstraintViolationInfo(ConstraintViolation constraintViolation){
            this.constraintClass = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType();
            this.fieldName = ValidationHelper.extractFieldNameFromPropertyPathStr(constraintViolation.getPropertyPath().toString());
            this.invalidValue = constraintViolation.getInvalidValue();
            this.message = constraintViolation.getMessage();
        }

        public Class getConstraintClass() {
            return constraintClass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getInvalidValue() {
            return invalidValue;
        }

        public String getMessage() {
            return message;
        }
    }
}