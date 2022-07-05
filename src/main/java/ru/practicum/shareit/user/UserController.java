package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable long id) {
        User user = userService.getUserById(id);

        return UserMapper.toUserDto(user);
    }

    @PostMapping
    public UserDto addUser(@RequestBody UserCreateDto userCreateDto) {
        User user = userService.addUser(
                UserMapper.toUser(userCreateDto)
        );

        return UserMapper.toUserDto(user);
    }

    @PutMapping
    public UserDto updateUser(@RequestBody UserDto userDto) {
        User user = userService.updateUser(
          UserMapper.toUser(userDto)
        );

        return UserMapper.toUserDto(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }
}
