package com.CaseTest.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Request {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private String processkey;

    @Column
    private String myvariable1;

    private String myvariable2;

    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProcesskey() {
        return processkey;
    }

    public void setProcesskey(String processkey) {
        this.processkey = processkey;
    }

    public String getMyvariable1() {
        return myvariable1;
    }

    public void setMyvariable1(String myvariable1) {
        this.myvariable1 = myvariable1;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMyvariable2() {
        return myvariable2;
    }

    public void setMyvariable2(String myvariable2) {
        this.myvariable2 = myvariable2;
    }
}

