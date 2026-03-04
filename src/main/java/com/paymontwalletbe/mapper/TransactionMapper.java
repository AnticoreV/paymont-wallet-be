package com.paymontwalletbe.mapper;

import com.paymont.wallet.api.model.TransactionResponse;
import com.paymontwalletbe.model.entities.TransactionEntry;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toResponse(TransactionEntry entry);

    default OffsetDateTime mapInstant(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
