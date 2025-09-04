package co.com.pragma.r2dbc.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import java.time.LocalDate;

@Table("users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    @Column("user_id")
    private Long id;
    private String name;
    private String lastName;
    private LocalDate birthDate;
    private String address;
    private String document;
    private String phone;
    private String email;
    private String password;
    private Integer roleId;
    private Integer baseSalary;



}