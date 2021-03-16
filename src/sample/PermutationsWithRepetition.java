package sample;

class PermutationsWithRepetition {
    private final String[] source;
    private final int variationLength;

    public PermutationsWithRepetition(String[] source, int variationLength) {
        this.source = source;
        this.variationLength = variationLength;
    }

    public String[][] getVariations() {
        int srcLength = source.length;
        int permutations = (int) Math.pow(srcLength, variationLength);

        String[][] table = new String[permutations][variationLength];

        for (int i = 0; i < variationLength; i++) {
            int t2 = (int) Math.pow(srcLength, i);
            for (int p1 = 0; p1 < permutations;) {
                for (Object o : source) {
                    for (int p2 = 0; p2 < t2; p2++) {
                        table[p1][i] = (String) o;
                        p1++;
                    }
                }
            }
        }
        return table;
    }
}