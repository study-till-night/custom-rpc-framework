package com.shuking.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player implements Serializable {

    private Long uid;

    private String userName;

    // private AccountInfo accountInfo;
    //
    // @Data
    // public static class AccountInfo implements Serializable{
    //
    //     private Date createTime;
    //
    //     private Double Store;
    // }
}
