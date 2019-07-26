package dum.springframework.aop.config;

import lombok.Data;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Data
public class DumAopConfig {
    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;

}
