package practical.task.orderservice.dto.response;

public record UserInfoDto(
        Long id,
        String email,
        String name,
        String surname
) {
}
