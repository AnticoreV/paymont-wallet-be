package com.paymontwalletbe.mapper;

import com.paymont.wallet.api.model.BalanceResponse;
import com.paymont.wallet.api.model.Currency;
import com.paymont.wallet.api.model.WalletResponse;
import com.paymontwalletbe.model.entities.Wallet;
import com.paymontwalletbe.model.entities.enums.CurrencyType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "walletId", source = "id")
    WalletResponse toResponse(Wallet wallet);

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "currency", source = "wallet.currency")
    @Mapping(target = "balance", source = "balance")
    BalanceResponse toBalanceResponse(Wallet wallet, BigDecimal balance);


    default Currency mapCurrency(CurrencyType currencyType) {
        return currencyType == null ? null : Currency.valueOf(currencyType.name());
    }

    default Double map(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    default OffsetDateTime mapInstant(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
