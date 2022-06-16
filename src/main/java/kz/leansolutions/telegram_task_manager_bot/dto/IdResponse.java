package kz.leansolutions.telegram_task_manager_bot.dto;

import lombok.Getter;

@Getter
public class IdResponse {
    private String id;

    public static IdResponse get(Long id) {
        return get(String.valueOf(id));
    }

    public static IdResponse get(String id) {
        IdResponse idResponse = new IdResponse();
        idResponse.id = id;
        return idResponse;
    }
}
