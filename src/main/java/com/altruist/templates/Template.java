package com.altruist.templates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Template {
    @Id
    public String id;
    @Column(columnDefinition = "text")
    public String text;
}
