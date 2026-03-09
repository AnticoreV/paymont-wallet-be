package com.paymontwalletbe.mapper;

import com.paymont.wallet.api.model.TransactionResponse;
import com.paymontwalletbe.model.entities.TransactionEntry;
import com.paymontwalletbe.model.entities.enums.TransactionStatus;
import com.paymontwalletbe.model.entities.enums.TransactionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "type", source = "transaction.type")
    @Mapping(target = "status", source = "transaction.status")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "createdAt", source = "createdAt")
    TransactionResponse toResponse(TransactionEntry entry);

    default TransactionResponse.TypeEnum map(TransactionType type) {
        if (type == null) return null;
        return TransactionResponse.TypeEnum.valueOf(type.name());
    }

    default String map(TransactionStatus status) {
        return status == null ? null : status.name();
    }

    default Double map(BigDecimal amount) {
        return amount == null ? null : amount.doubleValue();
    }

    default OffsetDateTime mapInstant(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
