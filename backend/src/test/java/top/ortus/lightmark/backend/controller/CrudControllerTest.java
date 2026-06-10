package top.ortus.lightmark.backend.controller;

import org.junit.jupiter.api.Test;
import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.service.GenericCrudService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrudControllerTest {

    @Test
    void deleteShouldUseParamsWhenPayloadIsNullOrEmpty() {
        GenericCrudService service = mock(GenericCrudService.class);
        when(service.delete(eq("user"), any())).thenReturn(true);

        CrudController controller = new CrudController(service);

        ApiResponse<Boolean> resp1 = controller.delete("user", null, Map.of("id", "2"));
        assertThat(resp1.getCode()).isEqualTo(0);
        assertThat(resp1.getData()).isTrue();
        verify(service).delete("user", Map.of("id", "2"));

        ApiResponse<Boolean> resp2 = controller.delete("user", Map.of(), Map.of("id", "2"));
        assertThat(resp2.getCode()).isEqualTo(0);
        assertThat(resp2.getData()).isTrue();
    }

    @Test
    void deleteShouldUsePayloadWhenPayloadProvided() {
        GenericCrudService service = mock(GenericCrudService.class);
        when(service.delete(eq("user"), any())).thenReturn(true);

        CrudController controller = new CrudController(service);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 2);
        ApiResponse<Boolean> resp = controller.delete("user", payload, Map.of("id", "999"));

        assertThat(resp.getCode()).isEqualTo(0);
        assertThat(resp.getData()).isTrue();
        verify(service).delete("user", payload);
    }
}

