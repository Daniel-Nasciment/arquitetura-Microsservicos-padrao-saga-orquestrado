package br.com.orchestratorservice.orchestratorservice.core.dto;

import br.com.orchestratorservice.orchestratorservice.core.enums.EEventSource;
import br.com.orchestratorservice.orchestratorservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class History {

    private EEventSource source;
    private ESagaStatus status;
    private String message;
    private LocalDateTime createdBy;

}
