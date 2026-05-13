package com.intranet.cic.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intranet.cic.entities.types.AlertSeverity;
import com.intranet.cic.entities.types.Category;
import com.intranet.cic.entities.types.Segment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "alert")
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "alertSeverity", nullable = false)
    private AlertSeverity alertSeverity;

    @Column(name = "date",nullable = true)
    private LocalDate date;

    @Column(name = "href",nullable = true)
    private String href;

    @Column(name = "flyerImage",nullable = true)
    private String flyerImage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties({"alerts", "news", "documents", "announcements",
            "events", "galleries", "members", "password",
            "hibernateLazyInitializer", "handler"})
    private User user;

}
