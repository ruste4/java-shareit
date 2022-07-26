package ru.practicum.shareit.item;

import lombok.*;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text")
    private String text;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @ToString.Exclude
    private Item item;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;

    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        return id != null && id.equals(((Comment) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
