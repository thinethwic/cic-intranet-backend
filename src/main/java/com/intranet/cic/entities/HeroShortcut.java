package com.intranet.cic.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intranet.cic.entities.types.HeroShortcutColor;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// A single quick-link button belonging to a HeroShortcutGroup folder.
@Entity
@Table(name = "hero_shortcuts")
@Data
public class HeroShortcut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "icon_name", nullable = false)
    private String iconName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HeroShortcutColor color;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"shortcuts"})
    private HeroShortcutGroup group;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
