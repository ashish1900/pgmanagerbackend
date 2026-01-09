package com.ash.main.dto;

import lombok.Data;

@Data
public class RoomAssignmentRequest {

	private String roomNumber;
	private String floorNumber;
    private String buildingNumber;
    private String address;
	
	
}
