package br.com.zup.chavepix.handler

import java.lang.RuntimeException

class ChavePixExistenteException(chave: String)
    : RuntimeException("A chave pix '${chave}' já foi utilizada")
