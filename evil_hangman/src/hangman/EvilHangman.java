package hangman;

import com.sun.source.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class EvilHangman {

    public static void main(String[] args) throws GuessAlreadyMadeException, EmptyDictionaryException, IOException {

        //arg[0] = txt file
        //arg[1] = length of word
        //arg[2] = number of guesses

        File inputFile = new File(args[0]);
        int guessesLeft = Integer.parseInt(args[2]);
        int the_word_length = Integer.parseInt(args[1]);
        EvilHangmanGame kalebsGame = new EvilHangmanGame();
        kalebsGame.startGame(inputFile, the_word_length);
        kalebsGame.setGuessesLeft(guessesLeft);




        while (guessesLeft > 0) {
            guessesLeft = kalebsGame.getGuessesLeft();
            System.out.println("You have " + guessesLeft + " guesses left");
            Set<Character> used_letters = kalebsGame.getGuessedLetters();
            System.out.println("Used letters: " + used_letters);
            String currentWord = kalebsGame.getPlayersWord();
            System.out.println("Word: " + currentWord);
            System.out.println("Enter guess: ");
            Scanner user_in = new Scanner(System.in);
            String input = user_in.next();
            char letter = input.charAt(0);
            if (!Character.isAlphabetic(letter)) {
                System.out.println("Invalid input!");
                continue;
            }
            try {
                kalebsGame.makeGuess(letter);
            }
            catch (GuessAlreadyMadeException e) {
                System.out.println("You already guessed that letter. Try again." + "\n");
                continue;
            }


            String result = kalebsGame.getPlayersWord();
            int helper = 0;
            for (int i = 0; i < result.length(); ++i) {
                if (result.charAt(i) == letter) {
                    helper += 1;
                }
            }
            if (helper > 0) {
                if (helper == 1) {
                    System.out.println("Yes, there is " + helper + " " + letter + "\n");
                }
                else {
                    System.out.println("Yes, there are " + helper + " " + letter + "'s" + "\n");
                }
            }
            else {
                System.out.println("Sorry, there are no " + letter + "'s" + "\n");
            }

            if (!result.contains("-")) {
                System.out.println("YOU WIN!!!");
                System.out.println("The word was: " + result);
                break;
            }

            guessesLeft = kalebsGame.getGuessesLeft();

            if (guessesLeft == 0) {
                System.out.println("You lose!");
                System.out.println("The word was: " + kalebsGame.getFinalWord());
            }
        }

    }

}
