package it.polito.ai.lab2.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.security.Timestamp;

@Entity
@Data
public class Token {

    @Id
    private String id;

    private Long teamId;

    private Timestamp expiryDate;
}
