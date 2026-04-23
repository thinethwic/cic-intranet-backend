package com.intranet.cic.entities;

import com.intranet.cic.entities.types.DocumentAccess;
import com.intranet.cic.entities.types.Category;
import com.intranet.cic.entities.types.DocumentType;
import com.intranet.cic.entities.types.Segment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "document")
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DocumentType type;              // e.g. PDF, WORD, EXCEL

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;       // e.g. HR, FINANCE, IT

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", nullable = false)
    private Segment segment;

    @Enumerated(EnumType.STRING)
    @Column(name = "access", nullable = false)
    private DocumentAccess access;         // e.g. PUBLIC, PRIVATE, RESTRICTED

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "allow_view", nullable = false)
    private Boolean allowView = true;

    @Column(name = "allow_download", nullable = false)
    private Boolean allowDownload = false;

//    // M side of "Create" relationship — many Documents created by one User
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // M side of right relationship — many Documents belong to many Members (if needed)

    @ManyToMany
    @JoinTable(
         name = "document_members",
         joinColumns = @JoinColumn(name = "document_id"),
         inverseJoinColumns = @JoinColumn(name = "member_id")
     )
     private List<Member> members;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
