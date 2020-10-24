package Deasciifier;

import Corpus.Sentence;
import Dictionary.Word;
import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParseList;
import Ngram.NGram;

import java.util.ArrayList;

public class NGramDeasciifier extends SimpleDeasciifier {
    private NGram<String> nGram;

    /**
     * A constructor of {@link NGramDeasciifier} class which takes an {@link FsmMorphologicalAnalyzer} and an {@link NGram}
     * as inputs. It first calls it super class {@link SimpleDeasciifier} with given {@link FsmMorphologicalAnalyzer} input
     * then initializes nGram variable with given {@link NGram} input.
     *
     * @param fsm   {@link FsmMorphologicalAnalyzer} type input.
     * @param nGram {@link NGram} type input.
     */
    public NGramDeasciifier(FsmMorphologicalAnalyzer fsm, NGram<String> nGram) {
        super(fsm);
        this.nGram = nGram;
    }

    /**
     * Checks the morphological analysis of the given word in the given index. If there is no misspelling, it returns
     * the longest root word of the possible analyses.
     * @param sentence Sentence to be analyzed.
     * @param index Index of the word
     * @return If the word is misspelled, null; otherwise the longest root word of the possible analyses.
     */
    private Word checkAnalysisAndSetRoot(Sentence sentence, int index){
        if (index < sentence.wordCount()){
            FsmParseList fsmParses = fsm.morphologicalAnalysis(sentence.getWord(index).getName());
            if (fsmParses.size() != 0){
                return fsmParses.getParseWithLongestRootWord().getWord();
            }
        }
        return null;
    }

    /**
     * The deasciify method takes a {@link Sentence} as an input. First it creates a String {@link ArrayList} as candidates,
     * and a {@link Sentence} result. Then, loops i times where i ranges from 0 to words size of given sentence. It gets the
     * current word and generates a candidateList with this current word then, it loops through the candidateList. First it
     * calls morphologicalAnalysis method with current candidate and gets the first item as root word. If it is the first root,
     * it gets its N-gram probability, if there are also other roots, it gets probability of these roots and finds out the
     * best candidate, best root and the best probability. At the nd, it adds the bestCandidate to the bestCandidate {@link ArrayList}.
     *
     * @param sentence {@link Sentence} type input.
     * @return Sentence result as output.
     */
    public Sentence deasciify(Sentence sentence) {
        Word word, bestRoot;
        Word previousRoot = null, root, nextRoot;
        String bestCandidate;
        FsmParseList fsmParses;
        double previousProbability, nextProbability, bestProbability;
        ArrayList<String> candidates;
        Sentence result = new Sentence();
        root = checkAnalysisAndSetRoot(sentence, 0);
        nextRoot = checkAnalysisAndSetRoot(sentence, 1);
        for (int i = 0; i < sentence.wordCount(); i++) {
            word = sentence.getWord(i);
            if (root == null){
                candidates = candidateList(word);
                bestCandidate = word.getName();
                bestRoot = word;
                bestProbability = 0;
                for (String candidate : candidates) {
                    fsmParses = fsm.morphologicalAnalysis(candidate);
                    root = fsmParses.getParseWithLongestRootWord().getWord();
                    if (previousRoot != null) {
                        previousProbability = nGram.getProbability(previousRoot.getName(), root.getName());
                    } else {
                        previousProbability = 0.0;
                    }
                    if (nextRoot != null) {
                        nextProbability = nGram.getProbability(root.getName(), nextRoot.getName());
                    } else {
                        nextProbability = 0.0;
                    }
                    if (Math.max(previousProbability, nextProbability) > bestProbability) {
                        bestCandidate = candidate;
                        bestRoot = root;
                        bestProbability = Math.max(previousProbability, nextProbability);
                    }
                }
                root = bestRoot;
                result.addWord(new Word(bestCandidate));
            } else {
                result.addWord(word);
            }
            previousRoot = root;
            root = nextRoot;
            nextRoot = checkAnalysisAndSetRoot(sentence, i + 2);
        }
        return result;
    }
}
