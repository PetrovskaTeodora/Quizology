package teodora.petrovska.quizology;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final int NUMBER_OF_ANIMALS_INCLUDED_IN_QUIZ = 10;

    private List<String> allAnimalsNamesList;
    private List<String> animalsNamesQuizList;

    // Set (Interface) cannot have duplicate values
    private Set<String> animalTypesInQuiz;
    private String correctAnimalAnswer;
    private int numberOfAllGuesses;
    private int numberOfRightAnswers;
    private int numberOfAnimalsGuessRows;
    private SecureRandom secureRandomNumber;
    private Handler handler;
    private Animation wrongAnswerAnimation;
    private LinearLayout quizologyLinearLayout;
    private TextView txtQuestionNumber;
    private ImageView imgAnimal;
    private LinearLayout[] rowsOfGuessButtonsInQuizology;
    private TextView txtAnswer;



    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_main, container, false);

        allAnimalsNamesList = new ArrayList<>();
        animalsNamesQuizList = new ArrayList<>();
        secureRandomNumber = new SecureRandom();
        handler = new Handler();

        wrongAnswerAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.wrong_answer_animation);
        wrongAnswerAnimation.setRepeatCount(1);


        quizologyLinearLayout = (LinearLayout) view.findViewById(R.id.quizologyLinearLayout);
        txtQuestionNumber = (TextView) view.findViewById(R.id.txtQuestionNumber);
        imgAnimal = (ImageView) view.findViewById(R.id.imgAnimal);

        rowsOfGuessButtonsInQuizology = new LinearLayout[3];
        rowsOfGuessButtonsInQuizology[0] = (LinearLayout) view.findViewById(R.id.firstRowLinearLayout);
        rowsOfGuessButtonsInQuizology[1] = (LinearLayout) view.findViewById(R.id.secondRowLinearLayout);
        rowsOfGuessButtonsInQuizology[2] = (LinearLayout) view.findViewById(R.id.thirdRowLinearLayout);
        txtAnswer = (TextView) view.findViewById(R.id.txtAnswer);

        for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

            for (int column = 0; column < row.getChildCount(); column++) {

                Button btnGuess = (Button) row.getChildAt(column);
                btnGuess.setOnClickListener(btnGuessListener);
                btnGuess.setTextSize(24);

            }
        }

        txtQuestionNumber.setText(getString(R.string.question_text, 1, NUMBER_OF_ANIMALS_INCLUDED_IN_QUIZ));

        return view;

    }

    private View.OnClickListener btnGuessListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Button btnGuess = ((Button) view);
            String guessValue = btnGuess.getText().toString();
            String answerValue = getTheExactAnimalName(correctAnimalAnswer);
            ++numberOfAllGuesses;

            // when user guess the right answer

            if (guessValue.equals(answerValue)) {
                ++numberOfRightAnswers;

                txtAnswer.setText(answerValue + "! " + "RIGHT");

                disableQuizGuessButton();

                if (numberOfRightAnswers == NUMBER_OF_ANIMALS_INCLUDED_IN_QUIZ) {

                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.results_string_value)
                            .setMessage(getString(R.string.results_string_value, numberOfAllGuesses,
                                    1000 / (double) numberOfAllGuesses))
                            .setPositiveButton(R.string.reset_animal_quiz, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    resetQuizology();


                                }
                            })
                            .setCancelable(false)
                            .show();
                    // user must click on reset the quiz
                    // animalQuizResults.setCancelable(false);
//                    animalQuizResults.show(getFragmentManager(), "AnimalQuizResults");

                }
                // when user choose wrong answer
                else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animateQuizology(true);
                        }

                    }, 1000);
                }
            } else {

                imgAnimal.startAnimation(wrongAnswerAnimation);

                txtAnswer.setText(R.string.wrong_answer_message);
                btnGuess.setEnabled(false);
            }
        }

    };

    private String getTheExactAnimalName(String animalName) {
        return animalName.substring(animalName.indexOf('-') + 1).replace('_', ' ');

    }

    private void disableQuizGuessButton() {
        for (int row = 0; row < numberOfAnimalsGuessRows; row++) {

            LinearLayout guessRowLinearLayout = rowsOfGuessButtonsInQuizology[row];
            for (int buttonIndex = 0; buttonIndex < guessRowLinearLayout.getChildCount(); buttonIndex++) {
                guessRowLinearLayout.getChildAt(buttonIndex).setEnabled(false);
            }
        }

    }

    public void resetQuizology() {

        AssetManager assets = getActivity().getAssets();
        allAnimalsNamesList.clear();

        // getting animal images and names from assets
        try {
            for (String animalType : animalTypesInQuiz) {

                String[] animalImagePathsInQuiz = assets.list(animalType);

                for (String animalImagePathInQuiz : animalImagePathsInQuiz) {

                    allAnimalsNamesList.add(animalImagePathInQuiz.replace(".png", ""));
                }
            }
        } catch (IOException e) {
            Log.e("Quizology", "Error", e);
        }

        numberOfRightAnswers = 0;
        numberOfAllGuesses = 0;
        animalsNamesQuizList.clear();

        int counter = 1;
        int numberOfAvailableAnimals = allAnimalsNamesList.size();

        while (counter <= NUMBER_OF_ANIMALS_INCLUDED_IN_QUIZ) {
            int randomIndex = secureRandomNumber.nextInt(numberOfAvailableAnimals);
            String animalImageName = allAnimalsNamesList.get(randomIndex);

            if (!animalsNamesQuizList.contains(animalImageName)) {
                animalsNamesQuizList.add(animalImageName);
                ++counter;
            }
        }
        showNextAnimal();
    }

    private void animateQuizology(boolean animateOutAnimalImage) {
        if (numberOfRightAnswers == 0) {

            return;
        }
        int xTopLeft = 0;
        int yTopLeft = 0;

        int xBottomRight = quizologyLinearLayout.getLeft() + quizologyLinearLayout.getRight();
        int yBottomRight = quizologyLinearLayout.getTop() + quizologyLinearLayout.getBottom();

        // Here is max value for radius
        int radius = Math.max(quizologyLinearLayout.getWidth(), quizologyLinearLayout.getHeight());

        Animator animator;

        if (animateOutAnimalImage) {

            animator = ViewAnimationUtils.createCircularReveal(quizologyLinearLayout,
                    xBottomRight, yBottomRight, radius, 0);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    showNextAnimal();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        } else {
            animator = ViewAnimationUtils.createCircularReveal(quizologyLinearLayout,
                    xTopLeft, yTopLeft, radius, 0);
        }

        animator.setDuration(700);
        animator.start();

    }

    private void showNextAnimal() {
        String nextAnimalImageName = animalsNamesQuizList.remove(0);
        correctAnimalAnswer = nextAnimalImageName;
        txtAnswer.setText("");

        txtQuestionNumber.setText(getString(R.string.question_text, numberOfRightAnswers + 1,
                NUMBER_OF_ANIMALS_INCLUDED_IN_QUIZ));

        String animalType = nextAnimalImageName.substring(0, nextAnimalImageName.indexOf("-"));

        AssetManager assets = getActivity().getAssets();

        // getting image fromassets folder and shoing it to the user
        try (InputStream stream = assets.open(animalType + "/" + nextAnimalImageName + ".png")) {

            Drawable animalImage = Drawable.createFromStream(stream, nextAnimalImageName);
            imgAnimal.setImageDrawable(animalImage);
            animateQuizology(false);
        } catch (IOException e) {
            Log.e("Quizology", "There is an error getting " + nextAnimalImageName, e);
        }

        Collections.shuffle(allAnimalsNamesList);

        // following 3 lines allAnimalsNamesList ma sy correctAnimalAnswer ka index correctAnimalNameIndex
        // ma save krati hain. Then other thing

        int correctAnimalNameIndex = allAnimalsNamesList.indexOf(correctAnimalAnswer);
        String correctAnimalName = allAnimalsNamesList.remove(correctAnimalNameIndex);
        allAnimalsNamesList.add(correctAnimalName);

        for (int row = 0; row < numberOfAnimalsGuessRows; row++) {

            // Enabling btns

            for (int column = 0; column < rowsOfGuessButtonsInQuizology[row].getChildCount();
                 column++) {

                Button btnGuess = (Button) rowsOfGuessButtonsInQuizology[row].getChildAt(column);
                btnGuess.setEnabled(true);

                // Showing animalsnames on btns

                String animalImageName = allAnimalsNamesList.get((row * 2) + column);
                btnGuess.setText(getTheExactAnimalName(animalImageName));
            }
        }
        //secureRandomNumber generates random nmbrs AND numberOfAnimalsGuessRows
        // shows number of animal guess rows
        //Here substituting one of the guess options with correct answer
        int row = secureRandomNumber.nextInt(numberOfAnimalsGuessRows);
        int column = secureRandomNumber.nextInt(2);
        LinearLayout randomRow = rowsOfGuessButtonsInQuizology[row];
        String correctAnimalImageName = getTheExactAnimalName(correctAnimalName);
        ((Button) randomRow.getChildAt(column)).setText(correctAnimalImageName);


    }


    public void modifyAnimalGuessRows(SharedPreferences sharedPreferences) {

        final String NUMBER_OF_GUESS_OPTIONS = sharedPreferences.getString(MainActivity.GUESSES, null);
        numberOfAnimalsGuessRows = Integer.parseInt(NUMBER_OF_GUESS_OPTIONS) / 2;

        for (LinearLayout horizontalLinearLayout : rowsOfGuessButtonsInQuizology) {
            horizontalLinearLayout.setVisibility(View.GONE);
        }

        for (int row = 0; row < numberOfAnimalsGuessRows; row++) {
            rowsOfGuessButtonsInQuizology[row].setVisibility(View.VISIBLE);

        }

    }

    public void modifyTypeOfAnimlasInQuiz(SharedPreferences sharedPreferences) {
        animalTypesInQuiz = sharedPreferences.getStringSet(MainActivity.ANIMALS_TYPE, null);
    }

    public void modifyQuizFont(SharedPreferences sharedPreferences) {

        String fontStringValue = sharedPreferences.getString(MainActivity.QUIZ_FONT, null);

        switch (fontStringValue) {

            case "chunkfive.otf":

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.chunkfive);
                    }
                }
                break;

            case "FontleroyBrown.ttf":

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.fontlerybrown);
                    }
                }
                break;

            case "Wonderbar Demo.otf":

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.wonderbarDemo);
                    }
                }
                break;

            case "waltograph42.ttf":

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setTypeface(MainActivity.waltograph);
                    }
                }
                break;
        }
    }

    public void modifyBGColor(SharedPreferences sharedPreferences) {

        String bgColor = sharedPreferences.getString(MainActivity.QUIZ_BACKGROUND_COLOR, null);

        switch (bgColor) {

            case "White":
                quizologyLinearLayout.setBackgroundColor(Color.WHITE);

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.BLUE);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;

            case "Black":
                quizologyLinearLayout.setBackgroundColor(Color.BLACK);

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.YELLOW);
                        button.setTextColor(Color.BLACK);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Green":
                quizologyLinearLayout.setBackgroundColor(Color.GREEN);

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.YELLOW);

                break;

            case "Blue":
                quizologyLinearLayout.setBackgroundColor(Color.BLUE);

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.RED);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Red":
                quizologyLinearLayout.setBackgroundColor(Color.RED);

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Yellow":
                quizologyLinearLayout.setBackgroundColor(Color.WHITE);

                for (LinearLayout row : rowsOfGuessButtonsInQuizology) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);
                    }
                }

                txtAnswer.setTextColor(Color.BLUE);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;


        }
    }
}
