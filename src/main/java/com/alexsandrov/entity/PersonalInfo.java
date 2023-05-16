package com.alexsandrov.entity;

import com.alexsandrov.converter.BirthdayConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PersonalInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 7881524354581126347L;
    private String firstname;
    private String lastname;
    @Convert(converter = BirthdayConverter.class)
    private Birthday birthDate;
}
