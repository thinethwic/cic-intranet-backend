package com.intranet.cic.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_categories")
@Data
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;


    @JsonProperty("cat_code")
    @Column(name = "cat_code", unique = true)
    private String catCode;

    @Column(name = "segment")
    private String segment;

    @Column(name = "department")
    private String department;

    private boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;
}