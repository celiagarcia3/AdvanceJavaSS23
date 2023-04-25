import java.util.ArrayList;

public class Sequence implements Comparable<Sequence>{
    ArrayList<Nucleotide> sequence;  //ArrayList that will contain the sequence of nucleotides

    //Constructor
    public Sequence(ArrayList<Nucleotide> sequence){
        this.sequence=sequence;
    }

    /*Method to get the reverse complement sequence of the sequence object. For that we create a new ArrayList
      "reverse" and add the complement of every nucleotide of the sequence itself but starting from the end. We use the
      complement method from the nucleotide class. */
    public ArrayList<Nucleotide> getReverseComplement(){
        ArrayList<Nucleotide> reverse= new ArrayList<>();
        for(int i= sequence.size() -1; i>=0; i--){
            reverse.add(sequence.get(i).complement());

        }

        return reverse;
    }

    /*Method to get or the fourmers of a sequence going nucleotide by nucleotide and saving itself and the next 3 one
      in an object of the class Fourmer. And then add the Fourmer to an ArrayList. */
    public ArrayList<Fourmer> get4mers() {
        ArrayList<Fourmer> fourmers = new ArrayList<>();

        for (int i = 0; i <= sequence.size() - 4; i++) {
            ArrayList<Nucleotide> fourmerSeq = new ArrayList<>();
            for (int j = i; j < i + 4; j++) {
                fourmerSeq.add(sequence.get(j));
            }

            Fourmer fourmer = new Fourmer(new Sequence(fourmerSeq));
            fourmers.add(fourmer.canonical());
        }
        return fourmers;
    }

    //Method to get the size of a sequence useful to iterate thought it.
    public int getSize(){
        return sequence.size();
    }


    //Method to get the value of a nucleotid of the sequence by giving a determined index.
    public Nucleotide getValue(int index){
        return sequence.get(index);
    }

    /* Method from the Comparable interface to compare the nucleotides of two different sequences and return a value < 0
       as soon as one nucleotide from this seq is smaller that the one from the extern one or >0 if it is larger. If
       the whole sequences have been studied and non of the return comands did run, the reult is 0 because all the
       nucleotides are equal */
    @Override
    public int compareTo(Sequence o) {
        for(int i=0; i< sequence.size(); i++){
            Nucleotide current= sequence.get(i);
            Nucleotide other = o.sequence.get(i);

            if(current.compareTo(other) < 0){
                return -1;
            }else if(current.compareTo(other) > 0){
                return 1;
            }

        }

        return 0;
    }
}
