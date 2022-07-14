package com.wallassouza.bibliotecaapi.api.resource.dto.bookdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnedLoanDTO {
    private Boolean returned;
}
