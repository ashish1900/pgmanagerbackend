package com.ash.main.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ash.main.dto.AllGuestsDto;
import com.ash.main.dto.GuestRequestDto;
import com.ash.main.dto.LeaveRquestDto;
import com.ash.main.dto.RoomAssignmentRequest;
import com.ash.main.entity.GuestDetailEntity;
import com.ash.main.entity.RequestStatus;
import com.ash.main.entity.RoomAssignment;
import com.ash.main.entity.StayRequestEntity;
import com.ash.main.entity.UserDetailsEntity;
import com.ash.main.rpository.RepositoryG;
import com.ash.main.rpository.RepositoryO;
import com.ash.main.rpository.RoomAssignmentRepo;
import com.ash.main.rpository.StayRequestRepository;

import jakarta.transaction.Transactional;



@Service
public class StayRequestService {


    @Value("${app.upload.dir}")
    private String uploadDir;

	
    @Autowired
    private StayRequestRepository stayRequestRepo;

    
    @Autowired
    private RepositoryG guestRepo;

    @Autowired
    private RepositoryO ownerRepo;
    
    @Autowired
    private RoomAssignmentRepo  roomAssignmentRepo;

    @Autowired
	private CloudinaryService cloudinaryService;
    

    @Transactional
    public StayRequestEntity sendRequestWithId(
            String guestMobile,
            Long ownerId,
            String idType,
            MultipartFile idFront,
            MultipartFile idBack,
            String tempAddress
    ) {

        GuestDetailEntity guest =
                guestRepo.findByMoNumber(guestMobile);
        UserDetailsEntity owner =
                ownerRepo.findById(ownerId)
                         .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        if (stayRequestRepo.existsByGuestIdAndOwnerId(guest.getId(), ownerId)) {
            throw new IllegalStateException("Request already sent");
        }

        //  Cloudinary upload
        String frontId = cloudinaryService.uploadPrivateImage(
                idFront,
                "pg-manager/id-image",
                "adhar_front_" + UUID.randomUUID()
        );

        String backId = cloudinaryService.uploadPrivateImage(
                idBack,
                "pg-manager/id-image",
                "adhar_back_" + UUID.randomUUID()
        );

        StayRequestEntity request = new StayRequestEntity();
        request.setGuest(guest);
        request.setOwner(owner);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        request.setIdType(idType);
        request.setIdFront(frontId); //  full public_id
        request.setIdBack(backId);
        request.setTempAddress(tempAddress);

        return stayRequestRepo.save(request);
    }

    
    
    
    
    
    
   
    
    
    @Transactional
    public String deletePgRequestByGuestIdAndOwnerId(Long guestId, Long ownerId) {

        StayRequestEntity request =
                stayRequestRepo.findByGuestIdAndOwnerId(guestId, ownerId);

        if (request == null) {
            throw new RuntimeException("No PG request found for deletion.");
        }

        try {
            //  DELETE ID IMAGES FROM CLOUDINARY
            if (request.getIdFront() != null && !request.getIdFront().isBlank()) {
                cloudinaryService.deleteAuthenticatedIdImage(request.getIdFront());
            }

            if (request.getIdBack() != null && !request.getIdBack().isBlank()) {
                cloudinaryService.deleteAuthenticatedIdImage(request.getIdBack());
            }

        } catch (Exception e) {
            // Log only – DB delete must continue
            System.out.println("Cloudinary image delete failed: " + e.getMessage());
        }

        //  DELETE DB ENTRY
        stayRequestRepo.delete(request);

        return "PG request deleted successfully";
    }

    
    
    
    
    
    
    @Transactional
    public boolean acceptAndAssign(Long requestId, Long ownerId, RoomAssignmentRequest dto) {
        UserDetailsEntity owner = ownerRepo.findById(ownerId).orElse(null);
        if (owner == null) return false;

        StayRequestEntity request = stayRequestRepo.findById(requestId).orElse(null);
        if (request == null || !request.getOwner().getId().equals(owner.getId())) {
            return false;
        }

        // 1. Accept request
        request.setStatus(RequestStatus.ACCEPTED);
        request.setRequestDate(LocalDateTime.now());
        stayRequestRepo.save(request);

		/*
		 * if (roomAssignmentRepo.existsByGuest(request.getGuest())) { throw new
		 * IllegalStateException("Guest already has a room assigned!"); }
		 */

     // 2. Check if already assigned by same owner
        if (roomAssignmentRepo.existsByGuestIdAndOwnerId(
                request.getGuest().getId(), owner.getId())) {
            throw new IllegalStateException("Guest already has a room assigned by this owner!");
        }
        
        
        // 3. Assign room
        RoomAssignment assignment = new RoomAssignment();
        assignment.setOwner(owner);
        assignment.setGuest(request.getGuest());
        assignment.setRoomNumber(dto.getRoomNumber());
        assignment.setFloorNumber(dto.getFloorNumber());
        assignment.setBuildingNumber(dto.getBuildingNumber());
        assignment.setAddress(dto.getAddress());

        roomAssignmentRepo.save(assignment);
        return true;
    }

    
    
    
   
    public List<Map<String, Object>> getAcceptedPGs(String guestMobile) {
        GuestDetailEntity guest = guestRepo.findByMoNumber(guestMobile);
        if (guest == null) {
            throw new IllegalArgumentException("Guest not found");
        }

        List<StayRequestEntity> acceptedRequests =
                stayRequestRepo.findAcceptedByGuestIdOrderByDateDesc(guest.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (StayRequestEntity req : acceptedRequests) {
            UserDetailsEntity owner = req.getOwner();

            Map<String, Object> pgInfo = new HashMap<>();
            pgInfo.put("pgName", owner.getPgName());
            pgInfo.put("city", owner.getCity() != null ? owner.getCity().getName() : "N/A");
            pgInfo.put("ownerMobile", owner.getMoNumber());
            pgInfo.put("ownerId", owner.getId());
            pgInfo.put("acceptedDate", req.getRequestDate());

            result.add(pgInfo);
        }

        return result;
    }

    
    public Map<String, Object> getOwnerDetailsForGuestAndOwner(String guestMobile, String ownerMobile) {
        GuestDetailEntity guest = guestRepo.findByMoNumber(guestMobile);
        if (guest == null) {
            return Map.of("status", "error", "message", "Guest not found");
        }

        // find accepted request for this guest + owner
        StayRequestEntity acceptedRequest = stayRequestRepo
                .findAcceptedRequest(guestMobile, ownerMobile)
                .orElse(null);

        if (acceptedRequest == null) {
            return Map.of("status", "error", "message", "No accepted request found for this PG");
        }

        UserDetailsEntity owner = acceptedRequest.getOwner();

        // room assignment
        RoomAssignment assignment = roomAssignmentRepo
                .findFirstByGuestIdAndOwnerId(guest.getId(), owner.getId())
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("ownerDetails", owner);

        if (assignment != null) {
            result.put("roomAssignment", Map.of(
                    "buildingNumber", assignment.getBuildingNumber(),
                    "floorNumber", assignment.getFloorNumber(),
                    "roomNumber", assignment.getRoomNumber(),
                    "address", assignment.getAddress()
            ));
        }

        return result;
    }
    
    
  
    
    
    public List<GuestRequestDto> getGuestRequests(Long ownerId) {
        
        return stayRequestRepo.findPendingGuestRequestsByOwnerId(ownerId);
    }
    
    
    
 public List<AllGuestsDto> getAllGuests(Long ownerId) {
        
        return stayRequestRepo.findAllGustsByOwnerId(ownerId);
    }

    
 
 
 
//Owner-specific delete
public void deleteByRequestForOwner(Long requestId) {
 
	StayRequestEntity req = stayRequestRepo.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
	
	stayRequestRepo.deleteById(requestId); 
}
 
 
 
 

public void markAsMismatch(Long requestId) {
    StayRequestEntity req = stayRequestRepo.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

    //  Update status
    req.setStatus(RequestStatus.MISMATCH);
    stayRequestRepo.save(req);

   
}
 

public boolean hasMismatchRequest(Long guestId) {
    return stayRequestRepo.existsByGuestIdAndStatus(guestId, RequestStatus.MISMATCH);
}




public void deleteMismatchRequest(Long guestId) {
    List<StayRequestEntity> mismatchRequests =
        stayRequestRepo.findByGuestIdAndStatus(guestId, RequestStatus.MISMATCH);

    if (!mismatchRequests.isEmpty()) {
        stayRequestRepo.deleteAll(mismatchRequests);
    }
}
  





public List<Map<String, Object>> getRequestsForGuest(String mobileNumber) {

    GuestDetailEntity guest = guestRepo.findByMoNumber(mobileNumber);
    if (guest == null) {
        throw new IllegalArgumentException("Guest not found");
    }

    List<Object[]> rows = stayRequestRepo.fetchFullStayHistory(guest.getId());

    return rows.stream()
        .map(row -> {

            StayRequestEntity req = (StayRequestEntity) row[0];
            LocalDateTime exitRequestDate = (LocalDateTime) row[1];
            LocalDateTime exitAcceptedDate = (LocalDateTime) row[2];

            Map<String, Object> map = new HashMap<>();

            map.put("requestId", req.getId());
            map.put("pgName", req.getOwner() != null ? req.getOwner().getPgName() : "Unknown PG");
            map.put("ownerName", req.getOwner() != null ? req.getOwner().getName() : "Unknown Owner");
            map.put("city", req.getOwner() != null && req.getOwner().getCity() != null ? req.getOwner().getCity() : "Unknown City");
            map.put("status", req.getStatus() != null ? req.getStatus().toString() : "PENDING");
            map.put("date", req.getRequestDate());
            map.put("idType", req.getIdType());
            map.put("idFront", req.getIdFront());
            map.put("idBack", req.getIdBack());
            map.put("mobile", req.getOwner() != null ? req.getOwner().getMoNumber() : "N/A");

            // ⭐ NEW FIELDS ADDED
            map.put("exitRequestDate", exitRequestDate);
            map.put("exitAcceptedDate", exitAcceptedDate);

            return map;
        })
        .toList();
}








public List<LeaveRquestDto> getLeaveRequest(Long ownerId) {
    
    return stayRequestRepo.findGuestsWithPendingLeaveRequests(ownerId);
}







    
}