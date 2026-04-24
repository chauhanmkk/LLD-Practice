package Walmart.Round1;

/**
 * building recommendation system
 * 1. Store the data
 *    HashMap<userId, List<Item>> -- add most recent old back to list
 * 2. PQ (score)
 *     Ranking system TF-IDF
 *     score = freq * weightage
 *
 * 3. Advance functionality
 *    (Graph)  product x -> product y -> p
 *
 *    Load spikes --> data lag --> loosing data
 *    eventual -> batching + scheduling -> kafka (backpressure) -> 10k -> process them  -> retry
 *    S -(srp)
 *    O -(ocp)
 *    L -(lsp)
 *    I (isp) ->
 *    D (dip) ->
 */
public class Solution2 {
}
