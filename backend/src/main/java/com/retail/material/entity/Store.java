package com.retail.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("store")
public class Store extends BaseEntity {

    private String storeCode;
    private String storeName;
    private String storeType;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private Integer status;
}