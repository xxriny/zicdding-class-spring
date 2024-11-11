package com.example.zicdding.domain.file.entity;

import com.example.zicdding.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Entity
@Table(name = "TB_FILE")
public class FileData extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long targetId;
    private String tableName;
    private String sort;
    private String path;
    private String originName;
    private String changedName;
    private Long size;
    private String ext;
    private String del_yn;
    @Builder
    public FileData(Long id, Long targetId, String tableName, String sort, String path, String originName, String changedName, Long size, String ext, String del_yn) {
        this.id = id;
        this.targetId = targetId;
        this.tableName = tableName;
        this.sort = sort;
        this.path = path;
        this.originName = originName;
        this.changedName = changedName;
        this.size = size;
        this.ext = ext;
        this.del_yn = del_yn;
    }
}
