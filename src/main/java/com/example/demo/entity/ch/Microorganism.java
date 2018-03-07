package com.example.demo.entity.ch;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Microorganism {
    @Id
    private String id;
}
