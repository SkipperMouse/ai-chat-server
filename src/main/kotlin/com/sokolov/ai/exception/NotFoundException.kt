package com.sokolov.ai.exception

class NotFoundException(id: Long) : RuntimeException(id.toString()) {
}