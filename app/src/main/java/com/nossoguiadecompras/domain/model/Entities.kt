package com.nossoguiadecompras.domain.model

data class CategoriaCount(
    val nome: String,
    val contador: Int,
    val temAnunciosGratis: Boolean = true
)

data class Anuncio(
    val id: String,
    val titulo: String,
    val descricao: String,
    val anunciante: String,
    val categoria: String,
    val tipo: TipoAnuncio = TipoAnuncio.GRATIS
)

enum class TipoAnuncio {
    GRATIS,
    PAGO,
    VIDEO,
    AUDIO
}
