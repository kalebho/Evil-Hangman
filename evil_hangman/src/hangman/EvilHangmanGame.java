package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {


    public String getPlayersWord() {
        return playersWord;
    }
    public int getGuessesLeft() {
        return guessesLeft;
    }
    public void setGuessesLeft(int guessesLeft) {
        this.guessesLeft = guessesLeft;
    }

    private Set<String> the_dictionary = new TreeSet<>();       //******
    private TreeMap<String, Set<String>> subsets = new TreeMap<>();     //******
    private SortedSet<Character> letters_guessed = new TreeSet<>();     //******
    private String playersWord = "";        //******
    private int guessesLeft = 0;        //******


    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {

        the_dictionary.clear();

        Scanner scan = new Scanner(dictionary);
        while (scan.hasNext()) {        //****************
            String word = scan.next();  //get next word in txt file     **************
            if (word.length() == wordLength) {      //**************
                the_dictionary.add(word);       //add word to dictionary set if right length
            }
        }
        if (the_dictionary.isEmpty()) {
            throw new EmptyDictionaryException();
        }

        //used to create the players word length with all '-'
        StringBuilder players_word = new StringBuilder();
        for (int i = 0; i < wordLength; i++) {
            players_word.append('-');
        }
        playersWord = players_word.toString();


    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {

        char lowerCaseGuess = Character.toLowerCase(guess);

        //throws exception if guess already made
        SortedSet<Character> letters_guessed = getGuessedLetters();         //*********
        if (letters_guessed.contains(lowerCaseGuess)) {
            throw new GuessAlreadyMadeException();      //**********
        }

        //add guessed letter to set letters_guessed
        letters_guessed.add(lowerCaseGuess);

        //if not then run the game by first partitioning each word in dictionary that is correct length
        //this will create subsets and add words to their lists or add to existing subsets
        partition(guess);
        the_dictionary = choosingNewDictionary(guess);
        return the_dictionary;

    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return letters_guessed;
    }

    // Used to get a random word from the final dictionary when the player looses
    public String getFinalWord() {
        for (String finalWord : the_dictionary) {
            return finalWord;
        }
        return null;
    }

    private String getSubsetKey(String word, char guessedLetter) {

        //Make string builder to make key...easy for appending '-' and letters
        StringBuilder key = new StringBuilder();

        //Loop through the input word and change non guessed letters to blank
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != guessedLetter) {      //check if letter is not same as guessedLetter
                key.append('-');      //if so replace letter
            } else {
                key.append(guessedLetter);      //else put in guessed
            }
        }

        return key.toString();  //return the key
    }

    private void partition(char guess) {

        //clear map each time you partition
        subsets = new TreeMap<>();

        //loop through all the words in the remaining dictionary
        for (String dictionaryWord : the_dictionary) {
            String subset_include = getSubsetKey(dictionaryWord, guess);      //get subset to work with based on input char and word in dictionary
            if (subsets.containsKey(subset_include)) {      //if the subset is already in the map       //***********
                subsets.get(subset_include).add(dictionaryWord);       //add the word to the subset_list
            } else {     //if not add the subset and its list of words
                Set<String> subset_list = new TreeSet<>();
                subset_list.add(dictionaryWord);
                subsets.put(subset_include, subset_list);
            }
        }
    }

    private Set<String> choosingNewDictionary(char guessedLetter) {

        // 1 CHOOSE SUBSET WITH BIGGEST SET SIZE

        // variable to find max size
        int max_size = 0;

        //loop used to find max size
        for (String key : subsets.keySet()) {
            if (subsets.get(key).size() > max_size) {        //check the size of all the sets in each key
                max_size = subsets.get(key).size();          //set max size to biggest value
            }
        }

        //used to store all the subsets with equally biggest lists
        Set<String> subset_biggest_lists = new TreeSet<>();

        for (String key : subsets.keySet()) {        //check all the subset lists sizes to see which are equal to the max_size
            if (subsets.get(key).size() == max_size) {
                subset_biggest_lists.add(key);
            }
        }

        //if there is only one with the biggest size then return that one
        if (subset_biggest_lists.size() == 1) {
            for (String return_key : subset_biggest_lists) {

                //LOGIC FOR DECREMENTING GUESSES IN GAME: ***BEGIN
                int the_numLetters = 0;
                for (int d = 0; d < return_key.length(); ++d) {
                    if (return_key.charAt(d) != '-') {
                        the_numLetters += 1;
                    }
                }
                if (the_numLetters == 0) {
                    guessesLeft -= 1;
                }
                //***END

                //THIS IS FOR UPDATE PLAYERS WORD: BEGIN***
                StringBuilder help_word_update = new StringBuilder();
                for (int i = 0; i < playersWord.length(); i++) {
                    //loop through both words same time
                    if (playersWord.charAt(i) != '-') {
                        help_word_update.append(playersWord.charAt(i));
                    } else {
                        help_word_update.append(return_key.charAt(i));
                    }
                }
                //update player word
                playersWord = help_word_update.toString();
                //***END

                return subsets.get(return_key);      //return list in that subset
            }
        }


        // 2 PICK SUBSET WITH FEWEST GUESSED LETTER IN IT

        // get all of the subsets in a list
        Set<String> similarSubsets = new TreeSet<String>();     //use for the subsets that have the similar length of guessed letters

        int minGuessed = 50;
        String returnKey = "";

        for (String subset : subset_biggest_lists) {
            int currVal = 0;

            //use to get subset with fewest guessed letters
            // int to count fewest number of guessed letters
            for (int n = 0; n < subset.length(); ++n) {     //Loop through all the letters and see how many are guessed letters
                if (subset.charAt(n) == guessedLetter) {
                    currVal += 1;
                }
            }
            if (currVal < minGuessed) {     //if found subset with fewer letters update the similarSubsets
                similarSubsets = new TreeSet<String>();     //wipe the set
                minGuessed = currVal;       //update minGuessed
                returnKey = subset;     //Save the key for return its set       //*******
                similarSubsets.add(subset);     //add the word to the similar subsets
            } else if (currVal == minGuessed) {      //if subsets are equal in amt of guessed letters then add the new subset to set
                similarSubsets.add(subset);
            }
        }

        //if there is only one subset we are looking at, then return that set of just one
        if (similarSubsets.size() == 1) {

            //LOGIC FOR DECREMENTING GUESSES IN GAME: ***BEGIN
            int the_numLetters = 0;
            for (int d = 0; d < returnKey.length(); ++d) {
                if (returnKey.charAt(d) != '-') {
                    the_numLetters += 1;
                }
            }

            if (the_numLetters == 0) {
                guessesLeft -= 1;
            }
            //***END

            //THIS IS FOR UPDATE PLAYERS WORD: BEGIN***
            StringBuilder help_word_update = new StringBuilder();
            for (int i = 0; i < playersWord.length(); i++) {
                //loop through both words same time
                if (playersWord.charAt(i) != '-') {
                    help_word_update.append(playersWord.charAt(i));
                } else {
                    help_word_update.append(returnKey.charAt(i));
                }
            }
            //update player word
            playersWord = help_word_update.toString();
            //***END

            return subsets.get(returnKey);
        } else {

            // 3 CHOOSE SUBSET WITH RIGHTMOST GUESSED LETTERS

            //make variables to compare rightmost
            int rightMost = 0;
            int currentValue = 0;
            String theReturnKey = "";

            //LOGIC FOR CHOOSING RIGHT MOST SUBSET: BEGIN***
            for (String this_subset : similarSubsets) {     //loop through the similar subsets
                currentValue = 0;
                for (int i = 0; i < this_subset.length(); i++) {    //loop through each letter
                    if (this_subset.charAt(i) == guessedLetter) {       //if character is equal to guessed letter than add up
                        currentValue += (i + 1);        //*************
                    }
                }
                if (currentValue > rightMost) {     //after looping through letters, check if the value is bigger than rightmost
                    rightMost = currentValue;       //update rightmost
                    theReturnKey = this_subset;     //the key to use for new dictionary is set      //**********
                }
                //***END


                //LOGIC FOR DECREMENTING GUESSES IN GAME: BEGIN***
                int numLetters = 0;
                for (int f = 0; f < theReturnKey.length(); ++f) {
                    if (theReturnKey.charAt(f) != '-') {
                        numLetters += 1;
                    }
                }

                if (numLetters == 0) {
                    guessesLeft -= 1;
                }
                //***END

                //THIS IS FOR UPDATE PLAYERS WORD: BEGIN***
                StringBuilder help_word_update = new StringBuilder();
                for (int i = 0; i < playersWord.length(); i++) {
                    //loop through both words same time
                    if (playersWord.charAt(i) != '-') {
                        help_word_update.append(playersWord.charAt(i));
                    } else {
                        help_word_update.append(theReturnKey.charAt(i));
                    }
                }
                //update player word
                playersWord = help_word_update.toString();
                //***END

                return subsets.get(theReturnKey);        //return the list of words in the subset that is chosen
            }
        }
        return null;
    }
}





