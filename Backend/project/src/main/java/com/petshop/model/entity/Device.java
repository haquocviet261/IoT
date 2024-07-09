package com.petshop.model.entity;

import com.petshop.common.constant.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long device_id;
    @Enumerated(EnumType.STRING)
    private DeviceType device_type;
    @OneToMany(mappedBy = "device",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<DeviceItem> deviceItemList;
}