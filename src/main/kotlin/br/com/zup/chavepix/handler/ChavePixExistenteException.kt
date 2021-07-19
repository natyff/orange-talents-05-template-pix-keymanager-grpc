package br.com.zup.chavepix.handler

import java.lang.RuntimeException

class ChavePixExistenteException(message: String)
    : RuntimeException(message)
