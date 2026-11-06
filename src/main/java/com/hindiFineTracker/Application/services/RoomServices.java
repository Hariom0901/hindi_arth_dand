package com.hindiFineTracker.Application.services;
import com.hindiFineTracker.Application.repository.RoomRepository;
import com.hindiFineTracker.Application.entity.Room;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service//tells spring that is class contains business logic
public class RoomServices {
   private final RoomRepository roomRepository;//allow services to save and search rooms
   public RoomServices(RoomRepository roomRepository){
       this.roomRepository = roomRepository;
   }
   public Room createRoom(String name){ //create room ,assing code and save to psql
       Room room = new Room();
       room.setName(name);
       room.setRoomCode(generateRoomCode());
       return roomRepository.save(room);
   }
    private String generateRoomCode() {  // generate room code and check wheather it is already exist;
        String code;

        do {
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (roomRepository.findByRoomCode(code).isPresent());

        return code;
    }
}
