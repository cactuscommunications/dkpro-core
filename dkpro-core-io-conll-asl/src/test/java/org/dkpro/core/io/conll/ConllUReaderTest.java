/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.conll;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertMorph;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertSentence;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConllUReaderTest
{
    @Test
    public void test()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ConllUReader.class, 
                ConllUReader.PARAM_LANGUAGE, "en",
                ConllUReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/u/", 
                ConllUReader.PARAM_PATTERNS, "conllu-en-orig.conll");
        
        JCas jcas = new JCasIterable(reader).iterator().next();

        String[] sentences = {
                "They buy and sell books.",
                "I have not a clue." };

        String[] posMapped = { "POS", "POS_VERB", "POS_CONJ", "POS_VERB", "POS_NOUN", "POS_PUNCT", "POS", "POS_VERB", "POS_ADV",
                "POS_DET", "POS_NOUN", "POS_PUNCT" };

        String[] posOriginal = { "PRN", "VB", "CC", "VB", "NNS", ".", "PRN", "VB", "RB", "DT", "NN",
                "." };

        String[] morphologicalFeeatures = {
                "[  0,  4]     -     -  Nom    -    -     -    -    -  Plur      -  -    -    -    -     -      -     - They (Case=Nom|Number=Plur)",
                "[  5,  8]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -  Pres      -     - buy (Number=Plur|Person=3|Tense=Pres)",
                "[ 13, 17]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -  Pres      -     - sell (Number=Plur|Person=3|Tense=Pres)",
                "[ 18, 23]     -     -    -    -    -     -    -    -  Plur      -  -    -    -    -     -      -     - books (Number=Plur)",
                "[ 25, 26]     -     -  Nom    -    -     -    -    -  Sing      -  1    -    -    -     -      -     - I (Case=Nom|Number=Sing|Person=1)",
                "[ 27, 31]     -     -    -    -    -     -    -    -  Sing      -  1    -    -    -  Pres      -     - have (Number=Sing|Person=1|Tense=Pres)",
                "[ 32, 35]     -     -    -    -    -     -    -  Neg     -      -  -    -    -    -     -      -     - not (Negative=Neg)",
                "[ 36, 37]     -     -    -    -    -     -    -    -     -      -  -    -  Art    -     -      -     - a (Definite=Ind|PronType=Art)",
                "[ 38, 42]     -     -    -    -    -     -    -    -  Sing      -  -    -    -    -     -      -     - clue (Number=Sing)"
        };
        
        assertSentence(sentences, select(jcas, Sentence.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertMorph(morphologicalFeeatures, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testDocumentID()
            throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en",
                ConllUReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/u_v2/",
                ConllUReader.PARAM_PATTERNS, "conllu-paragraph_and_document_boundaries.conll");

        JCas jcas = new JCasIterable(reader).iterator().next();

        AnnotationIndex<DocumentMetaData> index = jcas.getAnnotationIndex(DocumentMetaData.class);
        DocumentMetaData m = index.iterator().get();
        final String actualDocumentID = m.getDocumentId();
        final String expectedDocumentID = "mf920901-001";

        Assert.assertEquals("Document ID mismatch", expectedDocumentID, actualDocumentID);
    }

    @Test
    public void testMultipleDocumentIDs()
            throws Exception
    {
        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getRootLogger();
        logger.addAppender(appender);
        try {
            CollectionReaderDescription reader = createReaderDescription(
                    ConllUReader.class,
                    ConllUReader.PARAM_LANGUAGE, "en",
                    ConllUReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/u_v2/",
                    ConllUReader.PARAM_PATTERNS, "conllu-multiple_document_IDs.conll");

            JCas jcas = new JCasIterable(reader).iterator().next();

            AnnotationIndex<DocumentMetaData> index = jcas.getAnnotationIndex(DocumentMetaData.class);
            DocumentMetaData m = index.iterator().get();
            final String actualDocumentID = m.getDocumentId();
            final String expectedDocumentID = "mf920901-001;mf920901-002";

            Assert.assertEquals("Document ID mismatch", expectedDocumentID, actualDocumentID);
        }
        finally {
            logger.removeAppender(appender);
        }
        final List<LoggingEvent> log = appender.getLog();
        final LoggingEvent firstLogEntry = log.get(0);
        Assert.assertEquals(Level.WARN, firstLogEntry.getLevel());
        Assert.assertEquals("org.dkpro.core.io.conll.ConllUReader",
                firstLogEntry.getLoggerName());

        final String patternString = "File\\s[\\w:/%-\\.]+\\scontains\\smultiple\\sdocument\\sIDs:\\s"
                + "\\[mf920901-001,\\smf920901-002]";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(firstLogEntry.getMessage().toString());
        Assert.assertTrue(matcher.matches());
    }

    @Test
    public void testParagraphs()
            throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en",
                ConllUReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/u_v2/",
                ConllUReader.PARAM_PATTERNS, "conllu-multiple_paragraphs.conll");

        JCas jcas = new JCasIterable(reader).iterator().next();

        AnnotationIndex<Paragraph> index = jcas.getAnnotationIndex(Paragraph.class);

        List<String> paragraphIDs = new ArrayList<>();
        Iterator<Paragraph> iterator = index.iterator();
        while(iterator.hasNext()) {
            Paragraph p = iterator.next();
            paragraphIDs.add(p.getId());
        }
        List<String> expectedParagraphIDs = new ArrayList<String>(){{
            add("mf920901-001-p1");
            add("mf920901-001-p2");
        }};
        Assert.assertEquals(expectedParagraphIDs, paragraphIDs);

        final String expectedTextContent = "Slovenská ústava: pro i proti Slovenská ústava: pro i"
                + " proti\n"
                + "\n"
                + "Slovenská ústava: pro i proti";
        final String actualTextContent = jcas.getDocumentText();
        Assert.assertEquals(expectedTextContent, actualTextContent);

        String[] sentences = {
                "Slovenská ústava: pro i proti",
                "Slovenská ústava: pro i proti",
                "Slovenská ústava: pro i proti" };
        assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Test
    public void testEmptyNodes()
            throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en",
                ConllUReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/u_v2/",
                ConllUReader.PARAM_PATTERNS, "conllu-empty_nodes.conll");

        JCas jcas = new JCasIterable(reader).iterator().next();

        String[] sentences = {"Sue likes coffee and Bill tea"};

        assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    class TestAppender extends AppenderSkeleton {
        private final List<LoggingEvent> log = new ArrayList<>();

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(final LoggingEvent loggingEvent) {
            if (loggingEvent.getLevel().equals(Level.WARN)) {
                log.add(loggingEvent);
            }
        }

        @Override
        public void close() {
        }

        public List<LoggingEvent> getLog() {
            return new ArrayList<LoggingEvent>(log);
        }
    }
}
