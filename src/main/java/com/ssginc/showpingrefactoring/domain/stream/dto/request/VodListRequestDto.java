package com.ssginc.showpingrefactoring.domain.stream.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class VodListRequestDto {

    @Min(value = 0)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int pageNo;

    @Min(value = 0)
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long categoryNo;

    @Schema(allowableValues = {"mostView", "recent"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sort = "recent";

}
