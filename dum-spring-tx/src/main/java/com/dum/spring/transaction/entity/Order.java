package com.dum.spring.transaction.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.Date;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Table(name = "t_order")
@Data
public class Order {
    private Long id;
    @Column(name = "memberId")
    private Long mid;
    private String detail;
    private Date createTime;
}
