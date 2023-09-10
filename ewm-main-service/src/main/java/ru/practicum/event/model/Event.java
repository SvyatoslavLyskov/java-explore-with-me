package ru.practicum.event.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;
import ru.practicum.category.model.Category;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    Long id;
    @Column(length = 2000)
    String annotation;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;
    @Column(length = 120)
    String title;
    @Column(length = 7000)
    String description;
    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", nullable = false)
    Location location;
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    Boolean paid;
    @Column(name = "participant_limit")
    Long participantLimit;
    @Column(name = "request_moderation", columnDefinition = "BOOLEAN DEFAULT true")
    Boolean requestModeration;
    @Column(name = "published_on")
    LocalDateTime publishedOn;
    @Column(name = "created_on")
    LocalDateTime createdOn;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 9)
    State state;
    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    private Collection<Request> requests;

    public Long getConfirmedRequests() {
        if (this.getRequests() == null) {
            return 0L;
        }
        return this.getRequests().stream()
                .filter(request -> request.getStatus() == Status.CONFIRMED)
                .count();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Event event = (Event) o;
        return getId() != null && Objects.equals(getId(), event.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
