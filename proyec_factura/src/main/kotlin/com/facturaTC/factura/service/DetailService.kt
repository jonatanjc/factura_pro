package com.facturaTC.factura.service

import com.facturaTC.factura.model.Detail
import com.facturaTC.factura.repository.DetailRepository
import com.facturaTC.factura.repository.InvoiceRepository
import com.facturaTC.factura.repository.ProductRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DetailService {
    @Autowired
    lateinit var invoiceRepository: InvoiceRepository
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var detailRepository: DetailRepository

    fun list ():List<Detail>{
        return detailRepository.findAll()
    }

    fun save(detail: Detail):Detail{
        try {

            detail.invoiceId?.let { invoiceId ->
                if (!invoiceRepository.existsById(invoiceId)) {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found for id: $invoiceId")
                }
            }

            detail.productId?.let { productId ->
                if (!productRepository.existsById(productId)) {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for id: $productId")
                }
            }
            val response = detailRepository.save(detail)

            val product = productRepository.findById(detail.productId)
            product?.apply{
                stok = stok?.minus(detail.quantity!!)
            }
            productRepository.save(product!!)


            val listDetail = detailRepository.findByInvoiceId(detail.invoiceId)

            if (listDetail != null) {
                var suma = 0.0

                listDetail.forEach { element ->
                    suma += ((element.price ?: 0L).toInt() * (element.quantity ?: 0L).toInt())
                }

                val invoiceToUp = invoiceRepository.findById(detail.invoiceId)
                invoiceToUp?.apply {
                    total = suma.toDouble()
                }
                invoiceRepository.save(invoiceToUp!!)
            }

            return detailRepository.save(detail)

        } catch (ex: Exception) {

            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing the request", ex)
          }

    }

    fun update(detail: Detail): Detail {
        try {
            val existingDetail = detailRepository.findById(detail.id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "ID no existe")

            val originalQuantity = existingDetail.quantity!!
            val updatedQuantity = detail.quantity!!

            val quantityDifference = updatedQuantity - originalQuantity

            val product = productRepository.findById(existingDetail.productId)
            product?.apply {
                stok = stok?.plus(quantityDifference)
            }
            productRepository.save(product!!)

            existingDetail.quantity = updatedQuantity
            return detailRepository.save(existingDetail)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar el detalle", ex)
        }
    }
    fun updateName(detail: Detail): Detail{
        try{
            val response = detailRepository.findById(detail.id)
                ?: throw Exception("ID no existe")
            response.apply {
                quantity=detail.quantity //un atributo del modelo
            }
            return detailRepository.save(response)
        }
        catch (ex:Exception){
            throw ResponseStatusException(HttpStatus.NOT_FOUND,ex.message)
        }
    }

    fun delete(id: Long?): Boolean {
        try {
            val detail = detailRepository.findById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "ID no existe")

            val product = productRepository.findById(detail.productId)
            product?.apply {
                stok = stok?.plus(detail.quantity!!)
            }
            productRepository.save(product!!)

            detailRepository.deleteById(id!!)

            return true // Puedes ajustar el tipo de retorno según tus necesidades
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el detalle", ex)
        }
    }
    fun listById (id:Long?): Detail?{
        return detailRepository.findById(id)
    }
}
