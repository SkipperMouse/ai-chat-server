package com.sokolov.ai.utils

import org.springframework.core.io.Resource
import org.springframework.util.DigestUtils

fun Resource.contentHash(): String {
    return DigestUtils.md5DigestAsHex(inputStream)
}
