package com.sokolov.ai.utils

const val EMPTY = ""
const val UNKNOWN = "unknown"
const val ENRICHED_PROMPT = "ENRICHED_PROMPT"
const val ORIGINAL_PROMPT = "ORIGINAL_PROMPT"
const val QUESTION = "question"
const val CONTEXT = "context"
const val EMPTY_CONTEXT = "в контексте ничего не найдено"

const val CHUNK_SIZE = 250
const val MAX_DOCUMENTS_NUMBER_TO_TAKE_FROM_RAG = 4
const val RAG_DOCUMENT_QUALITY = 0.65


// LOGS
val FINAL_REQUEST_LOG_TEMPLATE = """
                FINAL REQUEST
            ----------------
            user:
            %s
""".trimIndent()

val FINAL_RESPONSE_LOG_HEADER = """
                FINAL RESPONSE
            ----------------
            text:
            %s

            META
            ----------------
            finishReason=%s
    
""".trimIndent()