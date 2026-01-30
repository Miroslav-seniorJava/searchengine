package searchengine.model;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "page")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;

    private int code;

    @Column(columnDefinition = "TEXT")
    private String content;
}
