package com.iot.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceItemRequest {
    private String device_name;
    private String mac_address;
    private Long device_id;
    private Long user_id;

}
