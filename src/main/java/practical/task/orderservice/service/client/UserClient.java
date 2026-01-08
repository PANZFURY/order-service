package practical.task.orderservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import practical.task.orderservice.dto.response.UserInfoDto;

@Component
@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

    @GetMapping("/api/user/get/{id}")
    UserInfoDto getById(@PathVariable Long id);

    @GetMapping("/api/user/get/by-email")
    UserInfoDto getByEmail(@RequestParam String email);
}
