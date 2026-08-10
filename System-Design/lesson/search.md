

## Why search is its own service

![Screenshot 2026-06-27 at 7.18.38 PM.png](../System-Design-Images/Screenshot%202026-06-27%20at%207.18.38%E2%80%AFPM.png)

Real text search means tokenizing input, ranking by relevance, handling typos and synonyms, and returning results in milliseconds. SQL LIKE does none of that, and even with full-text indexes Postgres tops out at modest scale.

The standard answer is a dedicated search engine: Elasticsearch, OpenSearch, Algolia, or Typesense, backed by an inverted index. Build search as a separate read-side that mirrors data from your primary database. Never search the primary directly past trivial scale.

## The inverted index

An inverted index maps each term to the list of documents containing it. "redis" -> [doc 1, doc 7, doc 422]. Searching becomes a quick set intersection across the query terms, which is why it is so fast.

- **Tokenization**, Break text into terms. Lowercase, strip punctuation, stem ('running' -> 'run'), remove stopwords, optionally apply language-specific analyzers.
- **Ranking**, Blend multiple signals: TF-IDF or BM25 for term relevance, freshness (how new is the doc), popularity (clicks, votes), and personalization (your history).
- **Sync from primary**, Change-data-capture (CDC) from the database, a Kafka pipeline, or scheduled jobs. Document-level eventual consistency is the rule. Searches see writes seconds-to-minutes after they land.

## Lexical vs semantic vs hybrid

- **Lexical (keyword)**, Fast, precise, predictable. The default. Great when users know what they are looking for. Elasticsearch and OpenSearch are built for this.
- **Semantic (vector)**, Embed the query and the documents into the same vector space, find nearest neighbors. Catches paraphrases and intent ("how do I cancel" matches "subscription termination").
- **Hybrid**, Run both, blend the scores. The current frontier and what almost every modern search-heavy product is moving toward (Notion, Linear, GitHub).
- S**pecialized vector stores**, Pinecone, Weaviate, Milvus for vectors only. pgvector if you want to keep one Postgres cluster instead of running two stacks.

## What search costs you

The index is a denormalized copy of your data. Updates are eventually consistent, and your CDC pipeline is now permanent infrastructure.

Reranking and personalization slow query time. Caching popular queries hides this from most users but burns memory.

Vector search is approximate (ANN). 100% recall is not achievable; query results are non-deterministic between runs.

Search relevance tuning is a constant project. Logging clicks, A/B testing rankers, curating synonym lists. It never really ends, and it is mostly a data problem, not a code problem.

Multi-tenant search means the auth scope is part of every query. Skip that and one tenant searches another's data.

## What to listen for

- **"Type-ahead" or "autocomplete"**, Separate prefix index, tight latency budget (under 50ms). Often a different store like Redis sorted sets or a small Elasticsearch cluster sized for it.
- **"Faceted search"**, Elasticsearch with aggregation buckets per facet (price range, category, rating).
- **"Semantic search" or "RAG over docs"**, Vector index, hybrid retrieval, reranker on top. Mention the embedding model and the chunking strategy.
- **"User-generated content at scale"**, Async indexing pipeline, eventual consistency, reranking layer. Be explicit about the ingestion lag.

State that the index is a separate read-side and how it stays in sync with the primary. That sentence alone signals seniority. Most candidates wave at "we use Elasticsearch" and stop.

## Questions

### An inverted index maps...

1) Each document to the full list of fields and values stored on it, in insertion order 
2) Each term to the list of documents containing it ✓
3) Vector embeddings to their k-nearest neighbors in a high-dimensional similarity space 
4) URLs to their cached HTML responses so subsequent reads skip the origin entirely

> An inverted index flips the relationship: term -> list of doc ids. 
> Searches become quick set intersections across the query terms.

### To handle queries like "how do I cancel" matching documents that say "subscription termination," which approach helps most?

1) Stricter tokenization that splits compound words and normalizes inflection more aggressively 
2) A larger lexical index that keeps every shingle and n-gram of every document body 
3) Semantic (vector) search or hybrid retrieval ✓
4) A hand-curated synonym list that maps every common cancellation phrase to a canonical term

> Lexical search misses paraphrases. 
> Semantic search embeds query and docs into the same space and finds similar meaning, not matching words. 
> Hybrid search blends both.

### What is the senior signal in a search-system answer?

1) Naming the most popular hosted search engine and walking through its pricing tiers 
2) Treating the index as a separate read-side and explaining how it stays in sync with the primary ✓
3) Avoiding any search engine and using SQL LIKE with a trigram index on the body column 
4) Always picking Algolia, regardless of corpus size, query mix, or latency requirements

> The CDC pipeline that keeps the search index in sync is the part most candidates skip. 
> Naming it explicitly shows seniority.

