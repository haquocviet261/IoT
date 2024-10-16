package com.iot.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Device")
public class Device extends CommonEntity{
    @Column(name = "name")
    private String name;
    @Column(name = "mac_address")
    private String mac_address;
    @Column(name = "user_id")
    private Long user_id;
}
