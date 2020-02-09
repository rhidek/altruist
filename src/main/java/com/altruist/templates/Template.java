package com.altruist.templates;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Template {
    @Id
    private String id;
    @Column(columnDefinition = "text")
    private String text;
}
