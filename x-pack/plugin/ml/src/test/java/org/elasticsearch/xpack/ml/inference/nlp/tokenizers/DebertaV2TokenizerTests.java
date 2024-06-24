/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.ml.inference.nlp.tokenizers;

import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.DebertaV2Tokenization;
import org.elasticsearch.xpack.core.ml.inference.trainedmodel.Tokenization;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class DebertaV2TokenizerTests extends ESTestCase {

    @Test
    public void numExtraTokensForSingleSequence() {}

    @Test
    public void getNumExtraTokensForSeqPair() {}

    @Test
    public void buildTokenizationResult() {}

    @Test
    public void requestBuilder() {}

    @Test
    public void createTokensBuilder() {}

    private static final List<String> TEST_CASE_VOCAB = List.of(
        DebertaV2Tokenizer.CLASS_TOKEN,
        DebertaV2Tokenizer.PAD_TOKEN,
        DebertaV2Tokenizer.SEPARATOR_TOKEN,
        DebertaV2Tokenizer.UNKNOWN_TOKEN,
        "▁Ela",
        "stic",
        "search",
        "▁is",
        "▁fun",
        "▁God",
        "z",
        "illa",
        "▁my",
        "▁little",
        "▁red",
        "▁car",
        "▁😀",
        "▁🇸🇴",
        DebertaV2Tokenizer.MASK_TOKEN,
        "."
    );
    private static final List<Double> TEST_CASE_SCORES = List.of(
        0.0,
        0.0,
        0.0,
        0.0,
        -12.535264015197754,
        -12.300995826721191,
        -13.255199432373047,
        -7.402246475219727,
        -11.201482772827148,
        -10.576351165771484,
        -7.898513317108154,
        -10.230172157287598,
        -9.18289566040039,
        -11.451579093933105,
        -10.858806610107422,
        -10.214239120483398,
        -10.230172157287598,
        -9.451579093933105,
        0.0,
        -3.0
    );

    private List<String> tokenStrings(List<? extends DelimitedToken> tokens) {
        return tokens.stream().map(DelimitedToken::toString).collect(Collectors.toList());
    }

    public void testTokenize() throws IOException {
        try (
            DebertaV2Tokenizer tokenizer = DebertaV2Tokenizer.builder(
                TEST_CASE_VOCAB,
                TEST_CASE_SCORES,
                new DebertaV2Tokenization(false, false, null, Tokenization.Truncate.NONE, -1)
            ).build()
        ) {
            TokenizationResult.Tokens tokenization = tokenizer.tokenize("Elasticsearch fun", Tokenization.Truncate.NONE, -1, 0, null)
                .get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁Ela", "stic", "search", "▁fun"));
            assertArrayEquals(new int[] { 4, 5, 6, 8 }, tokenization.tokenIds());
            assertArrayEquals(new int[] { 0, 1, 2, 3 }, tokenization.tokenMap());
        }
    }

    public void testSurrogatePair() throws IOException {
        try (
            DebertaV2Tokenizer tokenizer = DebertaV2Tokenizer.builder(
                TEST_CASE_VOCAB,
                TEST_CASE_SCORES,
                new DebertaV2Tokenization(false, false, null, Tokenization.Truncate.NONE, -1)
            ).build()
        ) {
            TokenizationResult.Tokens tokenization = tokenizer.tokenize("😀", Tokenization.Truncate.NONE, -1, 0, null).get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁\uD83D\uDE00"));

            tokenization = tokenizer.tokenize("Elasticsearch 😀", Tokenization.Truncate.NONE, -1, 0, null).get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁Ela", "stic", "search", "▁\uD83D\uDE00"));

            tokenization = tokenizer.tokenize("Elasticsearch 😀 fun", Tokenization.Truncate.NONE, -1, 0, null).get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁Ela", "stic", "search", "▁\uD83D\uDE00", "▁fun"));
        }
    }

    public void testMultiByteEmoji() throws IOException {
        try (
            DebertaV2Tokenizer tokenizer = DebertaV2Tokenizer.builder(
                TEST_CASE_VOCAB,
                TEST_CASE_SCORES,
                new DebertaV2Tokenization(false, false, null, Tokenization.Truncate.NONE, -1)
            ).build()
        ) {
            TokenizationResult.Tokens tokenization = tokenizer.tokenize("🇸🇴", Tokenization.Truncate.NONE, -1, 0, null).get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁🇸🇴"));
            assertThat(tokenization.tokenIds()[0], not(equalTo(3))); // not the unknown token

            tokenization = tokenizer.tokenize("🏁", Tokenization.Truncate.NONE, -1, 0, null).get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁🏁"));
            assertThat(tokenization.tokenIds()[0], equalTo(3)); // the unknown token (not in the vocabulary)
        }
    }

    public void testMultilingual() throws IOException {
        var vocab = DebertaV3TestVocab.loadMultiLingualTestVocab();

        try (
            DebertaV2Tokenizer tokenizer = DebertaV2Tokenizer.builder(
                TEST_CASE_VOCAB,
                TEST_CASE_SCORES,
                new DebertaV2Tokenization(false, false, null, Tokenization.Truncate.NONE, -1)
            ).setWithSpecialTokens(true).build()
        ) {
            for (int i = 0; i < DebertaV3TestVocab.MULTILINUGAL_TEXTS.length; i++) {
                logger.info(i);
                TokenizationResult.Tokens tokenization = tokenizer.tokenize(
                    DebertaV3TestVocab.MULTILINUGAL_TEXTS[i],
                    Tokenization.Truncate.FIRST,
                    -1,
                    0,
                    null
                ).get(0);
                assertArrayEquals(DebertaV3TestVocab.EXPECTED_TOKENS[i], tokenization.tokenIds());
            }
        }
    }

    public void testTokenizeWithNeverSplit() throws IOException {
        try (
            DebertaV2Tokenizer tokenizer = DebertaV2Tokenizer.builder(
                TEST_CASE_VOCAB,
                TEST_CASE_SCORES,
                new DebertaV2Tokenization(false, false, null, Tokenization.Truncate.NONE, -1)
            ).build()
        ) {
            TokenizationResult.Tokens tokenization = tokenizer.tokenize("Elasticsearch .<mask>.", Tokenization.Truncate.NONE, -1, 0, null)
                .get(0);
            assertThat(tokenStrings(tokenization.tokens().get(0)), contains("▁Ela", "stic", "search", "▁", ".", "<mask>", "▁", "."));
        }
    }

    public void testMultiSeqTokenization() throws IOException {
        try (
            DebertaV2Tokenizer tokenizer = DebertaV2Tokenizer.builder(
                TEST_CASE_VOCAB,
                TEST_CASE_SCORES,
                new DebertaV2Tokenization(false, false, null, Tokenization.Truncate.NONE, -1)
            ).setWithSpecialTokens(true).build()
        ) {
            TokenizationResult.Tokens tokenization = tokenizer.tokenize(
                "Elasticsearch is fun",
                "Godzilla my little red car",
                Tokenization.Truncate.NONE,
                0
            );

            var tokenStream = Arrays.stream(tokenization.tokenIds()).mapToObj(TEST_CASE_VOCAB::get).collect(Collectors.toList());
            assertThat(
                tokenStream,
                contains(
                    DebertaV2Tokenizer.CLASS_TOKEN,
                    "▁Ela",
                    "stic",
                    "search",
                    "▁is",
                    "▁fun",
                    DebertaV2Tokenizer.SEPARATOR_TOKEN,
                    DebertaV2Tokenizer.SEPARATOR_TOKEN,
                    "▁God",
                    "z",
                    "illa",
                    "▁my",
                    "▁little",
                    "▁red",
                    "▁car",
                    DebertaV2Tokenizer.SEPARATOR_TOKEN
                )
            );
        }
    }

}
