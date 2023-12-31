package com.facturaTC.factura.repository

import com.facturaTC.factura.model.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository: JpaRepository<Invoice, Long?> {
    fun findById (id: Long?): Invoice?

    @Query(nativeQuery = true)
    fun filterTotal (value: Double): List <Invoice>
}