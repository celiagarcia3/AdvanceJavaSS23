import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeMap;

public class Count4mers {
    public static void main (String[] args) {

        /* Get the sequence (as an arrayList of nucleotides) from a file using the method readFile
           described below and transform it into a Sequence object.*/
        Sequence fw = new Sequence(new ArrayList<>(readFile("gene.fna")));

        //Get all the 4mers in an ArrayList using the method get4mers (already gives the canonical form).
        ArrayList<Fourmer> fourmers = fw.get4mers();


        /* Create a map to store each unique 4mer (key) and its count (value). We use TreeMap so the keys will directly
           be sorted based on the method CompareTo of the class Fourmers*/

        TreeMap<Fourmer, Integer> count4mers = new TreeMap<>();

        /* Iterate over each 4-mer in the ArrayList fourmers with two nested for loops:

           - First loop goes through each fourmer and save it as the current one. If the variable "repeat" is
             true in the current fourmer it means it has already been saved in the map and the loop goes to
             the next interation (i +1). If it is still false, we save it in the map and start the second loop.

           - The second loop goes from the position i+1 till the end of the Array List. It saves the fourmer
             at the index position as the search one and check if it has been already saved (again with the repeat
             variable). If it is true, the second loop goes to the next iteration (j+1) but in the opposite case
             search and current fourmers are compared (using equalF() method from the Fourmer class) and in case
             they are equals, the count of the current fourmer increments one and the search fourmer variable "repeat
             is changed to true (using counted() method from Fourmer class) so we know in future iteration it is
             already saved. */

        for (int i = 0; i < fourmers.size(); i++) {
            Fourmer current = fourmers.get(i);
            if (!current.repeat) {
                count4mers.put(current, 1);
                for (int j = i + 1; j < fourmers.size(); j++) {
                    Fourmer search = fourmers.get(j);
                    if (!search.repeat) {
                        if (current.equalF(search)) {
                            count4mers.put(current, count4mers.get(current) + 1);
                            search.counted();
                        }
                    }
                }
            }
        }


        // Iterate over the map and print each unique 4-mer and its count
        for (Fourmer i : count4mers.keySet()) {
            i.printFourmer();
            System.out.println("\t" + count4mers.get(i));
        }
    }

    /* Method to read a fasta file and get the sequence of nucleotides in an ArrayList of objects from the class Nucleotide.
       It throws an exception in case the file is not founded or cannot be open */
    public static ArrayList<Nucleotide> readFile (String fileName){

        ArrayList<Nucleotide> sequence = new ArrayList<>();  //First we create the ArrayList
        boolean isHeader;                             //and a boolean variable helpful later to remove the header
        try {
            FileReader fr = new FileReader(fileName);        //Using the FileReader class

            int c;

            while ((c = fr.read()) != -1) {                    //-1 represents the end of the file
                if (c == '>') {
                    isHeader = true;
                    while (isHeader && (c = fr.read()) != -1) {  //While we are in the header we do not save anything
                        if (c == '\n') {                         //Until we got to the next line and change the variable
                            isHeader = false;                    //to false.
                        }
                    }
                } else {
                    if (Character.isLetter((char) c)) {                          // Check if c is a letter (avoid \n)
                        sequence.add(new Nucleotide(String.valueOf((char) c))); //Saving the nucleotides in an ArrayList
                    }
                }
            }

            fr.close();

        } catch (Exception e) {
            System.out.println(e);
        }

        return sequence;

    }




}
