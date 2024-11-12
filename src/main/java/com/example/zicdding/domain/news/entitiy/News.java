package com.example.zicdding.domain.news.entitiy;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "TB_ITNEWS")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String type;
    private String position;
    private int reward;
    private String rewardConfirm;
    private Date startedDate;
    private Date endDate;
    private String target;
    private String url;
    private String content;


}
