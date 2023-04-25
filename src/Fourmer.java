public class Fourmer implements Comparable<Fourmer>{
    Sequence seq;     //Object of the class Sequence containig the list of nucleotides.
    boolean repeat;   //Boolean variable helpful later for counting duplications.



    //Constructor
    public Fourmer(Sequence seq) {
        this.seq=seq;
        repeat=false;   //Repeat is initialize as false, till the fourmer is counted.
    }

    /* Method to check if the complement fourmer is lexicographically smaller then the one in the forward strand.
       It use the method get.ReverseComplement() from the Sequence class and get the complement/rev strand fourmer
       and compares each nucleotid from the beginning. Once a nucleotid from one fourmer is smaller lexicographically
       than the complementary, this one fourmer will be return. */
    public Fourmer canonical() {
        Sequence rev = new Sequence(seq.getReverseComplement());

        int i = 0;
        while (i < rev.getSize()) {
            Nucleotide n1 = seq.getValue(i);
            Nucleotide n2 = rev.getValue(i);
            if (n1.base.compareTo(n2.base) < 0) {
                return new Fourmer(seq);
            } else if (n1.base.compareTo(n2.base) > 0) {
                return new Fourmer(rev);
            } else {
                i++;
            }
        }

        // If the loop completes without finding any differences, return a default Fourmer object
        return new Fourmer(seq);
    }

    /* Method to compare the fourmer with another one and get true if they are equals 100%. As soon one nucleotid differs
       false is returned*/
    public boolean equalF(Fourmer other) {

        int i = 0;
        while (i < seq.getSize()) {
            Nucleotide n1 = seq.getValue(i);
            Nucleotide n2 = other.seq.getValue(i);
            if (!n1.base.equals(n2.base)) {
                return false;
            }

            i++;
        }

        return true;
    }

    //Method to print the fourmer sequence
    public void printFourmer(){
        for(int i=0; i< seq.getSize(); i++){
            System.out.print(seq.getValue(i).base);
        }
    }

    //Method to change the variable repeat to true, signaling that the fourmer is already being ocunted and saved in map.
    public void counted(){
        repeat=true;
    }

    /* Method from the Comparable interface to compare the sequencies of two different fourmers. For that we just use
       the compareTo method from the Sequence class and return the result. If < 0 this former is smaller lexicographically
       if > 0 is bigger. */
    @Override
    public int compareTo(Fourmer o) {
        Sequence current = this.seq;
        Sequence other = o.seq;

        return current.compareTo(other);
    }


}