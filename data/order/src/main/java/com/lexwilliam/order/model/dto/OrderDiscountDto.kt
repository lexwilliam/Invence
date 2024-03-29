package com.lexwilliam.order.model.dto

import com.lexwilliam.core.util.validateUUID
import com.lexwilliam.order.model.OrderDiscount

data class OrderDiscountDto(
    val uuid: String? = null,
    val name: String? = null,
    val fixed: Double? = null,
    val percent: Float? = null
) {
    fun toDomain() =
        OrderDiscount(
            uuid = uuid.validateUUID(),
            name = name ?: "",
            fixed = fixed ?: 0.0,
            percent = percent ?: 0.0f
        )

    companion object {
        fun fromDomain(domain: OrderDiscount) =
            OrderDiscountDto(
                uuid = domain.uuid.toString(),
                name = domain.name,
                fixed = domain.fixed,
                percent = domain.percent
            )
    }
}