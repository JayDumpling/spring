package com.dum.spring.transaction.entity;

import lombok.Data;

import javax.persistence.Table;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Table(name = "t_member")
@Data
public class Member {
    private Long id;
    private String name;
    private String addr;
    private Long age;
}
