public class Nucleotide implements Comparable<Nucleotide> {

    String base;  //instance variable with the value (letter) of the nucleotid

    //Constructor
    public Nucleotide(String base){
        this.base=base;
    }

    //Method to get the complement nucleotid of the object one.
    public Nucleotide complement(){
        switch (base){
            case "A": return new Nucleotide("T");
            case "T": return new Nucleotide("A");
            case "G": return new Nucleotide("C");
            case "C": return new Nucleotide("G");
        }

        return null;
    }

    /* Method from the Comparable interface to compare the bases of two different nucleotides and return a value < 0
       if this nucleotide is smaller then the other or value > 0 in the opposite case. If equals the result is 0 */
    @Override
    public int compareTo(Nucleotide o) {
        return base.compareTo(o.base);
    }
}
