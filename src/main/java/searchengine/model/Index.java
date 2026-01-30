package searchengine.model;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "search_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;

    @Column(name = "rank")
    private float rank;
}
