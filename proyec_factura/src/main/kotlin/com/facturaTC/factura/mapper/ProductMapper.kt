package com.facturaTC.factura.mapper

import com.facturaTC.factura.dto.ProductDto
import com.facturaTC.factura.model.Product

object ProductMapper {
    fun mapToDto(product: Product): ProductDto{
        return ProductDto(
            product.id,
            "${product.description} ${product.brand}"
        )
    }
}
