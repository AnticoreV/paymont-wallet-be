package com.paymontwalletbe.mapper;

import com.paymont.wallet.api.model.Currency;
import com.paymont.wallet.api.model.WalletResponse;
import com.paymontwalletbe.model.entities.Wallet;
import com.paymontwalletbe.model.entities.enums.CurrencyType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "walletId", source = "id")
    WalletResponse toResponse(Wallet wallet);

    default Currency mapCurrency(CurrencyType currencyType) {
        return Currency.valueOf(currencyType.name());
    }

    default OffsetDateTime mapInstant(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
