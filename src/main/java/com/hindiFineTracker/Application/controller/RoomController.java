package com.hindiFineTracker.Application.controller;
import com.hindiFineTracker.Application.repository.RoomRepository;
import com.hindiFineTracker.Application.entity.Room;
import com.hindiFineTracker.Application.controller.RoomController;
import com.hindiFineTracker.Application.services.RoomServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomServices roomServices;
    public RoomController(RoomServices roomServices){
        this.roomServices = roomServices;
    }
    @PostMapping
    public Room createRoom(@RequestBody CreateRoomRequest request){
       return roomServices.createRoom(request.name());
    }
    public record CreateRoomRequest(String name){};
}
//This creates the endpoint:
//POST /api/rooms
//When you send a room name, the backend creates the room and returns its details, including the room code.