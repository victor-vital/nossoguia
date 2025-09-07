package com.nossoguiadecompras.data.mock

import com.nossoguiadecompras.domain.model.Anuncio
import com.nossoguiadecompras.domain.model.CategoriaCount
import com.nossoguiadecompras.domain.model.TipoAnuncio

object MockData {

  // Contadores das categorias (conforme imagem)
  const val contadorSupermercados = 121
  const val contadorDMaisLojas = 0
  const val contadorUtilidadePublica = 0

  // Categorias
  val categorias = listOf(
    CategoriaCount(
      nome = "SUPERMERCADOS",
      contador = contadorSupermercados,
      temAnunciosGratis = true
    ),
    CategoriaCount(
      nome = "D+ LOJAS",
      contador = contadorDMaisLojas,
      temAnunciosGratis = true
    ),
    CategoriaCount(
      nome = "UTILIDADE PÚBLICA",
      contador = contadorUtilidadePublica,
      temAnunciosGratis = true
    ),
    CategoriaCount(
      nome = "COMÉRCIO NOSSO",
      contador = 0,
      temAnunciosGratis = false
    )
  )

  // Lista de anúncios mock para o carrossel
  val anuncios = listOf(
    Anuncio(
      id = "1",
      titulo = "Promoção Supermercado ABC",
      descricao = "Descontos especiais em frutas e verduras frescas",
      anunciante = "Supermercado ABC",
      categoria = "SUPERMERCADOS",
      tipo = TipoAnuncio.GRATIS
    ),
    Anuncio(
      id = "2",
      titulo = "Farmácia Popular",
      descricao = "Medicamentos com os melhores preços da cidade",
      anunciante = "Farmácia Popular",
      categoria = "UTILIDADE PÚBLICA",
      tipo = TipoAnuncio.GRATIS
    ),
    Anuncio(
      id = "3",
      titulo = "Loja de Roupas Fashion",
      descricao = "Coleção de verão com até 50% de desconto",
      anunciante = "Fashion Store",
      categoria = "D+ LOJAS",
      tipo = TipoAnuncio.VIDEO
    ),
    Anuncio(
      id = "4",
      titulo = "Padaria do Bairro",
      descricao = "Pães fresquinhos todos os dias das 6h às 20h",
      anunciante = "Padaria Central",
      categoria = "COMÉRCIO NOSSO",
      tipo = TipoAnuncio.GRATIS
    ),
    Anuncio(
      id = "5",
      titulo = "Eletrônicos Tech",
      descricao = "Smartphones, tablets e notebooks com preços imperdíveis",
      anunciante = "Tech Store",
      categoria = "D+ LOJAS",
      tipo = TipoAnuncio.AUDIO
    )
  )

  // Dados mock para o cabeçalho (chips)
  const val chipVisualizacoes1 = "123.456"
  const val chipLocalizacao = "MANAUS"
  const val chipVisualizacoes2 = "123.456.789"

  // Nome do anunciante principal (dinâmico)
  const val nomeAnunciante = "NOME DO ANUNCIANTE"
}
