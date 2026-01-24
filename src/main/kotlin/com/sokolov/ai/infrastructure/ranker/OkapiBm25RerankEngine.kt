package com.sokolov.ai.infrastructure.ranker

import com.github.pemistahl.lingua.api.Language
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.springframework.ai.document.Document
import kotlin.math.ln

class OkapiBm25RerankEngine {

    fun rerank(corpus: List<Document>, query: String, limit: Int): List<Document> {
        if (corpus.isEmpty()) return emptyList()

        val corpusStats = computeCorpusStats(corpus)
        if (corpusStats.totalDocs == 0) return emptyList()

        val queryTerms = tokenize(query)
        val documentScoreMap = mutableMapOf<Document, Double>()
        return corpus
            .sortedByDescending { doc -> score(doc, corpusStats, queryTerms, documentScoreMap)}
            .take(limit)
    }

    private fun computeCorpusStats(corpus: List<Document>): CorpusStats {
        var totalDocs = 0
        var totalLength = 0
        val tokenizedDocs = mutableMapOf<Document, List<String>>()
        val docFreq = mutableMapOf<String, Int>()

        for (doc in corpus) {
            val tokens = tokenize(doc.text ?: continue)
            if (tokens.isEmpty()) continue

            tokenizedDocs[doc] = tokens
            totalLength += tokens.size
            totalDocs++

            val uniqueTerms = tokens.toSet()
            for (term in uniqueTerms) {
                docFreq[term] = docFreq.getOrDefault(term, 0) + 1
            }
        }
        val avgDocLength = if (totalDocs == 0) 0.0 else (totalLength / totalDocs.toDouble())
        return CorpusStats(
            totalDocs = totalDocs,
            docFreq = docFreq,
            tokenizedDocs = tokenizedDocs,
            avgDocLength = avgDocLength
        )
    }

    private fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        val analyzer = detectLanguageAnalyzer(text)
        analyzer.tokenStream(null, text).use { stream ->
            val attr = stream.getAttribute(CharTermAttribute::class.java)
            stream.reset()
            while (stream.incrementToken()) {
                tokens.add(attr.toString())
            }
            stream.end()
        }
        return tokens
    }

    private fun detectLanguageAnalyzer(text: String): Analyzer {
        val lang = languageDetector.detectLanguageOf(text)
        return when (lang) {
            Language.RUSSIAN -> RussianAnalyzer()
            else -> EnglishAnalyzer()
        }
    }

    private fun score(
        doc: Document,
        stats: CorpusStats,
        queryTerms: List<String>,
        documentScoreMap: MutableMap<Document, Double>
    ): Double {
        val existingScore = documentScoreMap[doc]
        if (existingScore != null) return existingScore

        val tokens = stats.tokenizedDocs[doc] ?: return 0.0
        if (stats.avgDocLength == 0.0) return 0.0
        val termFrequencyMap = mutableMapOf<String, Int>()
        for (token in tokens) {
            termFrequencyMap[token] = termFrequencyMap.getOrDefault(token, 0) + 1
        }

        val docLength = tokens.size
        var score = 0.0

        for (term in queryTerms) {
            val tf = termFrequencyMap.getOrDefault(term, 0)
            val df = stats.docFreq.getOrDefault(term, 0)

            val idf = ln(1 + (stats.totalDocs - df + 0.5) / (df + 0.5))

            val numerator = tf * (K + 1)
            val denominator = tf + K * (1 - B + B * docLength / stats.avgDocLength)
            score += idf * (numerator / denominator)
        }
        documentScoreMap[doc] = score
        return score
    }

    //    todo java doc
    companion object {
        private val languageDetector = LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.RUSSIAN).build()
        private const val K = 1.2
        private const val B = 0.75
    }

    data class CorpusStats(
        val docFreq: Map<String, Int>,
        val tokenizedDocs: Map<Document, List<String>>,
        val avgDocLength: Double,
        val totalDocs: Int
    )
}