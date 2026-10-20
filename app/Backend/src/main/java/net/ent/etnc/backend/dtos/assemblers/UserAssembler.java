package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.UserDto;
import net.ent.etnc.backend.dtos.requests.StagiaireRequest;
import net.ent.etnc.backend.models.entities.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class UserAssembler {

    public UserDto toDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .nid(user.getNid())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .dateNaissance(user.getDateNaissance())
                .grade(user.getGrade())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }

    public List<UserDto> toDtos(Collection<User> users){
        return users.stream().map(this::toDto).toList();
    }

    public User toEntity(UserDto userDto){
        User user = new User();

        user.setId(userDto.getId());
        user.setNid(userDto.getNid());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setDateNaissance(userDto.getDateNaissance());
        user.setGrade(userDto.getGrade());
        user.setRole(userDto.getRole());
        user.setActive(userDto.getActive());

        return user;
    }

    public User toEntity(StagiaireRequest stagiaireRequest){
        User user = new User();

        user.setNom(stagiaireRequest.getNom());
        user.setPrenom(stagiaireRequest.getPrenom());
        user.setNid(stagiaireRequest.getNid());
        user.setEmail(stagiaireRequest.getEmail());
        user.setDateNaissance(stagiaireRequest.getDateNaissance());
        user.setGrade(stagiaireRequest.getGrade());

        return user;
    }
}
