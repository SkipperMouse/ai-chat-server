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

    /**
     * Reranks a collection of documents based on their relevance to the query using BM25 algorithm.
     *
     * @param corpus The list of documents to rank
     * @param query The search query string
     * @param limit Maximum number of documents to return
     * @return List of top-ranked documents sorted by relevance score (highest first)
     */
    fun rerank(corpus: List<Document>, query: String, limit: Int): List<Document> {
        if (corpus.isEmpty()) return emptyList()

        val corpusStats = computeCorpusStats(corpus)
        if (corpusStats.totalDocs == 0) return emptyList()

        val queryTerms = tokenize(query)
            .distinct()
        return corpus
            .map { doc -> doc to score(doc, corpusStats, queryTerms) }
            .sortedByDescending { (_, score) -> score }
            .take(limit)
            .map { (doc, _) -> doc }
    }

    private fun computeCorpusStats(corpus: List<Document>): CorpusStats {
        var totalDocs = 0
        var totalLength = 0
        val docStats = mutableMapOf<Document, DocStats>()
        val termInDocsFrequency = mutableMapOf<String, Int>()

        for (doc in corpus) {
            val tokens = tokenize(doc.text ?: continue)
            if (tokens.isEmpty()) continue

            val currentDocStats = calculateDocStats(tokens)
            docStats[doc] = currentDocStats
            for (term in currentDocStats.termFrequency.keys) {
                termInDocsFrequency[term] = termInDocsFrequency.getOrDefault(term, 0) + 1
            }
            totalLength += currentDocStats.length
            totalDocs++
        }
        val avgDocLength = if (totalDocs == 0) 0.0 else (totalLength / totalDocs.toDouble())

        return CorpusStats(
            totalDocs = totalDocs,
            avgDocLength = avgDocLength,
            termInDocsFrequency = termInDocsFrequency,
            docStats = docStats,
        )
    }

    private fun calculateDocStats(tokens: List<String>): DocStats {
        val termFrequency = mutableMapOf<String, Int>()
        for (token in tokens) {
            termFrequency[token] = termFrequency.getOrDefault(token, 0) + 1
        }
        return DocStats(termFrequency, tokens.size)
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
            Language.RUSSIAN -> russianAnalyzer.get()
            else -> englishAnalyzer.get()
        }
    }

    /**
     * Calculates the BM25 relevance score for a document given a query.
     *
     * The score is computed using the BM25 formula:
     * ```
     * score = Σ(IDF(term) * (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * |D| / avgdl)))
     * ```
     *
     * Where:
     * - IDF: Inverse Document Frequency (measures term rarity)
     * - tf: Term frequency in the document
     * - |D|: Document length
     * - avgdl: Average document length in the corpus
     * - k1, b: Tuning parameters (see companion object)
     *
     * @param doc The document to score
     * @param stats Corpus statistics
     * @param queryTerms List of unique query terms
     * @return BM25 relevance score (higher is more relevant)
     */
    private fun score(
        doc: Document,
        stats: CorpusStats,
        queryTerms: List<String>,
    ): Double {
        if (stats.avgDocLength == 0.0) return 0.0

        val docStats = stats.docStats[doc] ?: return 0.0
        val termFrequencyMap = docStats.termFrequency
        val docLength = docStats.length

        var docScore = 0.0
        // prevents long documents from being ranked higher just because they are long
        val lengthNormalization = K * (1 - B + B * docLength / stats.avgDocLength)

        for (term in queryTerms) {
            val termFrequency = termFrequencyMap.getOrDefault(term, 0)
            if (termFrequency == 0) continue

            // how many documents contains the term
            val docFrequency = stats.termInDocsFrequency.getOrDefault(term, 0)
            // how rare the term is? More rare -> more important
            val idf = ln(1 + (stats.totalDocs - docFrequency + 0.5) / (docFrequency + 0.5))

            val numerator = termFrequency * (K + 1)
            val denominator = termFrequency + lengthNormalization
            docScore += idf * (numerator / denominator)
        }
        return docScore
    }

    companion object {
        /** Language detector for automatic language identification */
        private val languageDetector = LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.RUSSIAN).build()

        /**
         * K (k1): Term-frequency saturation parameter.
         * Higher values mean repeated terms matter more.
         * Typical range: 1.2-2.0
         */
        private const val K = 1.2

        /**
         * B: Length normalization parameter.
         * 0 = no normalization (document length doesn't matter)
         * 1 = full normalization (heavily penalize long documents)
         * Typical value: 0.75
         */
        private const val B = 0.75

        /** Thread-local Russian text analyzer for tokenization */
        private val russianAnalyzer = ThreadLocal.withInitial { RussianAnalyzer() }

        /** Thread-local English text analyzer for tokenization */
        private val englishAnalyzer = ThreadLocal.withInitial { EnglishAnalyzer() }
    }

    /**
     * Statistics for the entire document corpus.
     *
     * @property termInDocsFrequency Maps each term to the number of documents containing it
     * @property docStats Maps each document to its individual statistics
     * @property avgDocLength Average length of documents in the corpus
     * @property totalDocs Total number of documents in the corpus
     */
    data class CorpusStats(
        val termInDocsFrequency: Map<String, Int>,
        val docStats: Map<Document, DocStats>,
        val avgDocLength: Double,
        val totalDocs: Int
    )

    /**
     * Statistics for a single document.
     *
     * @property termFrequency Maps each term to its frequency in the document
     * @property length Total number of tokens in the document
     */
    data class DocStats(
        val termFrequency: Map<String, Int>,
        val length: Int
    )
}