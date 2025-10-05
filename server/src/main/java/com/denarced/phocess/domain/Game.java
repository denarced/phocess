package com.denarced.phocess.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity
@Table(name = "t_game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_name", nullable = false, length = 64)
    private String name;

    @Column(name = "c_date", nullable = false)
    private LocalDate date;

    @Column(name = "c_count_total")
    private Long totalCount;

    @Column(name = "c_count_first")
    private Long firstCount;

    @Column(name = "c_count")
    private Long count;
}
