package tir.alex.doors.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper=true)
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column
    private String name;

    @Column(nullable = false)
    private String role = "USER";

    @Column
    private LocalDate createdAt;
}
